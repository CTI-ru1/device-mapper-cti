package net.sparkworks.mapper.service;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class SenderService {
    private static final String MESSAGE_TEMPLATE = "%s,%f,%d";

    @Value("${rabbitmq.queue.send}")
    String rabbitQueueSend;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Async
    public void sendMeasurement(final String uri, final Double reading, final long timestamp) {
        final String message = String.format(MESSAGE_TEMPLATE, uri, reading, timestamp);
        rabbitTemplate.send(rabbitQueueSend, rabbitQueueSend, new Message(message.getBytes(), new MessageProperties()));
    }
}
