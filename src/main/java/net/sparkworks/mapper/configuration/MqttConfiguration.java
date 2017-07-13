package net.sparkworks.mapper.configuration;

import net.sparkworks.mapper.service.SenderService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class MqttConfiguration {
    /**
     * LOGGER.
     */
    private static final Logger LOGGER = Logger.getLogger(MqttConfiguration.class);

    @Value("${mqtt.url}")
    private String mqttUrl;
    @Value("${mqtt.topics}")
    private String mqttTopics;
    @Value("${mqtt.clientId}")
    private String mqttClientId;

    @Autowired
    private SenderService senderService;

    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageProducer inbound() {
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(mqttUrl, mqttClientId, mqttTopics);
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(0);
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }


    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handler() {
        return new MessageHandler() {

            @Override
            public void handleMessage(Message<?> message) throws MessagingException {
                String topic = (String) message.getHeaders().get("mqtt_topic");

                if (topic.startsWith("s") || topic.equals("heartbeat") || topic.equals("stats"))
                    return;
                parseMessage(topic, message);

            }
        };
    }

    private void parseMessage(final String topic, final Message<?> message) {
        final String payload = (String) message.getPayload();
        try {
            List<ParsedReading> readings = parseStringMessage(topic, payload);
            for (ParsedReading reading : readings) {
                if (reading != null) {
                    senderService.sendMeasurement(reading.getUri(), reading.getValue(), System.currentTimeMillis());
                }
            }
        } catch (Exception e) {
            LOGGER.error(e, e);
        }
    }

    private static List<ParsedReading> parseStringMessage(final String topic, final String payload) {
        if (topic.startsWith("flare")) {
            return parsePlainMessage(topic, payload);
        } else {
            return parseComplexMessage(topic, payload);
        }
    }

    private static List<ParsedReading> parsePlainMessage(final String uri, final String value) {
        final List<ParsedReading> readings = new ArrayList<>();
        LOGGER.info(String.format("%s : %s", uri, value));
        readings.add(new ParsedReading(uri, new Double(value)));
        return readings;
    }

    private static List<ParsedReading> parseComplexMessage(final String topic, final String payload) {
        List<ParsedReading> readings = new ArrayList<>();
        if (payload.contains(",") && payload.contains("+") && payload.split("\\+").length >= 3) {

            final String mac = payload.split("/", 2)[0];

            final String sensorsPayload = payload.substring(payload.indexOf('/') + 1);
            LOGGER.info("sensorsPayload:" + sensorsPayload);
            for (final String part : sensorsPayload.split("\\+")) {
                if (part.isEmpty() || !part.contains(","))
                    continue;
                final String sensorName = part.split(",")[0];
                final String sensorValue = part.split(",")[1];
                final String uri = String.format("%s/%s/%s", topic, mac, sensorName);
                LOGGER.info(String.format("%s : %s", uri, sensorValue));
                readings.add(new ParsedReading(uri, new Double(sensorValue)));
            }
        }
        return readings;
    }

    private static class ParsedReading {
        private final String uri;
        private final Double value;

        public ParsedReading(final String uri, final Double value) {
            this.uri = uri;
            this.value = value;
        }

        public String getUri() {
            return uri;
        }

        public Double getValue() {
            return value;
        }
    }
}