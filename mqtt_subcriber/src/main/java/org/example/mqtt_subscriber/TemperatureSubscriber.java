package org.example.mqtt_subscriber;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class TemperatureSubscriber {
    @Value("${mqtt.topic}")
    private String topic;
    @Value("${mqtt.broker.url}")
    private String brokerUrl;
    @Value("${mqtt.username}")
    private String username = "mqtt_user";
    @Value("${mqtt.password}")
    private String password = "mqtt_password";
    @PostConstruct
    public void startListening() {
        try{
            MqttClient client = new MqttClient(brokerUrl, topic);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setUserName(username);
            options.setPassword(password.toCharArray());
            options.setSocketFactory(SSLSocketFactoryGenerator.getSocketFactory());
            client.connect(options);
            log.info("Connected to HiveMQ broker");

            client.subscribe(topic, (messageTopic, message) -> {
                String temp = new String(message.getPayload());
                log.info("Received temperature: {}°C", temp);
            });
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }

    }
}
