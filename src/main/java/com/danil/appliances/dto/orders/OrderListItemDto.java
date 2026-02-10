package com.danil.appliances.dto.orders;

import com.danil.appliances.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderListItemDto {

    private Long id;

    private String clientName;

    private String employeeName;

    private OrderStatus orderStatus;

    private BigDecimal amount;
}

