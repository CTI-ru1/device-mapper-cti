package net.sparkworks.mapper.configuration;

import org.apache.log4j.Logger;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfiguration {
    /**
     * LOGGER.
     */
    private static final Logger LOGGER = Logger.getLogger(RabbitConfiguration.class);

    @Value("${rabbitmq.server}")
    String rabbitServer;
    @Value("${rabbitmq.port}")
    String rabbitPort;
    @Value("${rabbitmq.username}")
    String rabbitUser;
    @Value("${rabbitmq.password}")
    String rabbitPassword;

    @Bean
    ConnectionFactory connectionFactory() {
        LOGGER.info(rabbitServer + ":" + rabbitPort);
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(rabbitServer);
        connectionFactory.setPort(Integer.parseInt(rabbitPort));
        connectionFactory.setUsername(rabbitUser);
        connectionFactory.setPassword(rabbitPassword);
        connectionFactory.setVirtualHost("/");
        return connectionFactory;
    }
}