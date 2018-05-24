package com.events;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class DeviceEventBus {

    private static final String QUEUE_NAME = "device";
    private static DeviceEventBus instance;
    private Channel channel = null;

    public DeviceEventBus() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = null;
        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    public void publish(Object data) {
        try {
            channel.basicPublish("", QUEUE_NAME, null, data.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static DeviceEventBus getInstance() {
        if (instance == null) {
            instance = new DeviceEventBus();
        }
        return instance;
    }
}
