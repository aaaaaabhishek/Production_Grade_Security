package com.example.customSe.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.session")
@Getter
@Setter
public class AppSessionProperties {

    private String creationPolicy;
    private int maxSessions;
    private boolean preventNewLogin;
}
