package org.example.mqtt_subscriber;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLContext;

public class SSLSocketFactoryGenerator {
    public static SSLSocketFactory getSocketFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, null, null);
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

