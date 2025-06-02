package org.example.mqtt_publisher;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

@Component
@Slf4j
@RequiredArgsConstructor
public class Publisher implements MqttCallback {
    private final String topic = "temperature";
    private final String clientId = "publisherClient";
    private final String brokerUrl = "tcp://127.0.0.1:1883";
    private MqttClient client;
    private MqttConnectOptions options;
    private Timer timer;

    @PostConstruct
    public void startPublishing(){
        try{
            client = new MqttClient(brokerUrl, clientId);
            client.setCallback(this);

            options = new MqttConnectOptions();
            options.setAutomaticReconnect(false);

            client.connect(options);
            log.info("Connected to Mosquitto broker");

            onlineMessagePublish();
            temperaturePublish();
        } catch (MqttException e) {
            log.error("Failed to connect to broker:", e);
        }
    }

    private void onlineMessagePublish() {
        try{
            String onlineMessage = "Publisher is online";
            MqttMessage message = new MqttMessage(onlineMessage.getBytes());
            message.setQos(2);
            message.setRetained(true);
            client.publish(topic, message);
            log.info("Published online message");
        }catch (MqttException e){
            log.error("Failed to publish online message:", e);
        }
    }

    private void temperaturePublish() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!client.isConnected()) {
                    log.warn("Client disconnected. Skipping publish.");
                    return;
                }
                int temperature = new Random().nextInt(16) + 15;
                String message = String.valueOf(temperature);
                try {
                    client.publish(topic, message.getBytes(), 2, false);
                    log.info("Published temperature: {}°C", temperature);
                } catch (MqttException e) {
                    log.error("Failed to publish", e);
                }
            }
        }, 0, 5000);
    }

    @PreDestroy
    public void disconnect() {
        try {
            if (client != null && client.isConnected()) {
                String offlineMessage = "Publisher is offline";
                MqttMessage message = new MqttMessage(offlineMessage.getBytes());
                message.setQos(2);
                message.setRetained(true);
                client.publish(topic, message);
                log.info("Published retained offline message");

                client.disconnect(1000);
                client.close();
                log.info("Disconnected from Mosquitto broker");
            }
        } catch (MqttException e) {
            log.error("Failed to disconnect from broker:", e);
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.error("Connection lost: {}", cause.getMessage());
        while (true) {
            try {
                Thread.sleep(3000);
                client.connect(options);
                log.info("Reconnected to broker");

                onlineMessagePublish();
                break;
            } catch (Exception e) {
                log.error("Reconnection failed. Retrying...", e);
            }
        }
    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }
}
