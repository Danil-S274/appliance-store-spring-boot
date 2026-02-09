package com.danil.appliances.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ClientDto {

    private Long id;

    private String name;

    private String email;

    private Boolean enabled;

    private String cardLast4;

    private BigDecimal balance;
}

