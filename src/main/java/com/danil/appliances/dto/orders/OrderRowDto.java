package com.danil.appliances.dto.orders;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderRowDto {

    private Long id;

    private String applianceName;

    private long number;

    private BigDecimal amount;
}
