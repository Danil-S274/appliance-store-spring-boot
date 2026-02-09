package com.danil.appliances;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ApplianceStoreSpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApplianceStoreSpringApplication.class, args);
    }

}
