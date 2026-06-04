package com.helphub.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.payos")
public class PayOsProperties {
    private String clientId;
    private String apiKey;
    private String checksumKey;
}
