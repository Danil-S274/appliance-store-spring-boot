package com.danil.appliances.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class OrderCreateDto {

    @NotNull
    @Positive
    private Long clientId;

    @NotNull
    @Positive
    private Long employeeId;
}
