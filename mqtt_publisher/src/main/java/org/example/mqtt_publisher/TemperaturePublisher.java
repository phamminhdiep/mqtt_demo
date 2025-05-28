package org.example.mqtt_publisher;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

@Component
@Slf4j
@RequiredArgsConstructor
public class TemperaturePublisher {
    @Value("${mqtt.topic}")
    private String topic;
    @Value("${mqtt.broker.url}")
    private String brokerUrl;
    @Value("${mqtt.username}")
    private String username = "mqtt_user";
    @Value("${mqtt.password}")
    private String password = "mqtt_password";
    private MqttClient mqttClient;

    @PostConstruct
    public void startPublishing(){
        try{
            mqttClient = new MqttClient(brokerUrl, MqttClient.generateClientId());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setUserName(username);
            options.setPassword(password.toCharArray());
            options.setSocketFactory(SSLSocketFactoryGenerator.getSocketFactory());
            mqttClient.connect(options);
            log.info("Connected to HiveMQ broker");

            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    int temperature = new Random().nextInt(16) + 15; // 15°C to 30°C
                    String message = String.valueOf(temperature);
                    try {
                        mqttClient.publish(topic, new MqttMessage(message.getBytes()));
                        log.info("Published temperature: {}°C", temperature);
                    } catch (MqttException e) {
                        log.error("Failed to publish", e);
                    }
                }
            }, 0, 5000);
        } catch (MqttException e) {
            log.error("Failed to connect to broker:", e);
        }
    }

}
