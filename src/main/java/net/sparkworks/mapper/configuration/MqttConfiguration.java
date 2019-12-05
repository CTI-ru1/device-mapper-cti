package net.sparkworks.mapper.configuration;

import lombok.extern.slf4j.Slf4j;
import net.sparkworks.mapper.adapter.CustomMqttChannelAdapter;
import net.sparkworks.mapper.service.SenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Configuration
public class MqttConfiguration {
    private static final String VALUE_SEPARATOR = ",";
    private static final String READING_SEPARATOR = "+";
    private static final String READING_SEPARATOR_REGEX = "\\+";
    private static final String MAC_SEPARATOR = "/";
    
    @Value("${mqtt.url}")
    private String mqttUrl;
    @Value("${mqtt.topics}")
    private String mqttTopics;
    @Value("${mqtt.clientId}")
    private String mqttClientId;
    @Value("${mqtt.username}")
    private String mqttUsername;
    @Value("${mqtt.password}")
    private String mqttPassword;

    @Autowired
    private SenderService senderService;

    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }
    
    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        factory.setServerURIs(mqttUrl);
        if (mqttUsername != null && !mqttUsername.isEmpty()) {
            factory.setUserName(mqttUsername);
            factory.setPassword(mqttPassword);
        }
        
        return factory;
    }
    
    @Bean
    public MessageProducer inbound() {
        final CustomMqttChannelAdapter adapter = new CustomMqttChannelAdapter(mqttClientId, mqttClientFactory(), mqttTopics);
        adapter.setRecoveryInterval(5000);
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
            long now = System.currentTimeMillis();
            //cleanup non printable characters
            final List<ParsedReading> readings = parseStringMessage(topic, payload.replaceAll("[\\p{C}]", ""));
            for (final ParsedReading reading : readings) {
                if (reading != null) {
                    senderService.sendMeasurement(reading.getUri(), reading.getValue(), now);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    static List<ParsedReading> parseStringMessage(final String topic, final String payload) {
        log.info("[" + topic + "] '" + payload + "'");
        if (topic.startsWith("flare")) {
            return parsePlainMessage(topic, payload);
        } else {
            if (payload.contains(READING_SEPARATOR)) {
                return parseComplexMessage(topic, payload.substring(0, payload.lastIndexOf(READING_SEPARATOR) + 1));
            } else {
                return parseComplexMessage(topic, payload);
            }
        }
    }

    private static List<ParsedReading> parsePlainMessage(final String uri, final String value) {
        final List<ParsedReading> readings = new ArrayList<>();
        log.info(String.format("%s : %s", uri, value));
        readings.add(new ParsedReading(uri, new Double(value)));
        return readings;
    }

    protected static List<ParsedReading> parseComplexMessage(final String topic, final String payload) {
        List<ParsedReading> readings = new ArrayList<>();
        if (!payload.contains(VALUE_SEPARATOR)) {
            return Collections.emptyList();
        }
        if (!payload.contains(READING_SEPARATOR) || (payload.split(READING_SEPARATOR_REGEX).length >= 3 || payload.split(READING_SEPARATOR_REGEX).length == 1)) {
            final String mac = payload.split(MAC_SEPARATOR, 2)[0];
        
            final String sensorsPayload = payload.substring(payload.indexOf('/') + 1);
            log.info("sensorsPayload [" + topic + "]" + sensorsPayload);
            for (final String part : sensorsPayload.split(READING_SEPARATOR_REGEX)) {
                if (part.isEmpty() || !part.contains(","))
                    continue;
                final String sensorName = part.split(VALUE_SEPARATOR)[0];
                final String sensorValue = part.split(VALUE_SEPARATOR)[1];
                final String uri = String.format("%s/%s/%s", topic, mac, sensorName);
                log.info(String.format("%s : %s", uri, sensorValue));
                readings.add(new ParsedReading(uri, new Double(sensorValue)));
            }
        }
        return readings;
    }

    static class ParsedReading {
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