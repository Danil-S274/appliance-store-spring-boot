package com.danil.appliances.dto.orders;

import com.danil.appliances.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailsDto {

    private Long id;

    private String clientName;

    private String employeeName;

    private OrderStatus orderStatus;

    private BigDecimal amount;

    private List<OrderRowDto> rows;
}
