package org.example.mqtt_subscriber;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class Subscriber implements MqttCallback {

    private final String topic = "temperature";
    private final String clientId = "subscriberClient";
    private final String brokerUrl = "tcp://127.0.0.1:1883";
    private MqttClient client;
    private MqttConnectOptions options;

    @PostConstruct
    public void startListening() {
        try{
            client = new MqttClient(brokerUrl, clientId);

            options = new MqttConnectOptions();
            options.setCleanSession(false);

            client.connect(options);
            log.info("Connected to broker");

            client.setCallback(this);
            client.subscribe(topic, 2);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }

    }

    @PreDestroy
    public void disconnect() {
        try {
            if (client != null && client.isConnected()) {
                client.disconnect();
                log.info("Disconnected from Mosquitto broker");
            }
        } catch (MqttException e) {
            log.error("Failed to disconnect from broker:", e);
        }
    }

    public static boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.error(cause.getMessage());
        try {
            Thread.sleep(2000);
            client.connect(options);
            log.info("Reconnected successfully");
        } catch (MqttException | InterruptedException e) {
            log.error("Reconnection failed: {}", e.getMessage());
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String messageContent = new String(message.getPayload());
        if(isNumeric(messageContent)) {
            log.info("Received temperature: {}°C", new String(message.getPayload()));
            return;
        }
        log.info(messageContent);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }
}
