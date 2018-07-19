package net.sparkworks.mapper.adapter;


import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;

public class CustomMqttChannelAdapter extends MqttPahoMessageDrivenChannelAdapter {
    
    public CustomMqttChannelAdapter(String url, String clientId, MqttPahoClientFactory clientFactory, String... topic) {
        super(url, clientId, clientFactory, topic);
    }
    
    public CustomMqttChannelAdapter(String clientId, MqttPahoClientFactory clientFactory, String... topic) {
        super(clientId, clientFactory, topic);
    }
    
    public CustomMqttChannelAdapter(String url, String clientId, String... topic) {
        super(url, clientId, topic);
    }
    
    @Override
    public void connectionLost(Throwable cause) {
        super.connectionLost(cause);
        System.exit(1);
    }
}
