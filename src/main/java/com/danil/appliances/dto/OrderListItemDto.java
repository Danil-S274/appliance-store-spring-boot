package com.danil.appliances.dto;

import com.danil.appliances.model.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderListItemDto {

    private Long id;

    private String clientName;

    private String employeeName;

    private OrderStatus orderStatus;

    private BigDecimal amount;
}

