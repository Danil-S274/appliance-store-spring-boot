package com.danil.appliances.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderRowDto {

    private Long id;

    private String applianceName;

    private long number;

    private BigDecimal amount;
}
