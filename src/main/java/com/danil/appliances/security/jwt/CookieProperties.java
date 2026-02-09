package com.danil.appliances.security.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.cookies")
public class CookieProperties {
    private boolean secure = true;
    private String sameSite = "Lax";
    private String domain;
}
