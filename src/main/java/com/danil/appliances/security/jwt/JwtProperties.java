package com.danil.appliances.security.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Data
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private String secret;

    private Duration accessTtl = Duration.ofMinutes(15);

    private Duration refreshTtl = Duration.ofDays(14);

    private String accessCookie = "ACCESS_TOKEN";
    private String refreshCookie = "REFRESH_TOKEN";
}
