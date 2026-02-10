package com.danil.appliances.dto.account;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CheckoutRequestDto {

    @NotBlank(message = "Recipient name is required")
    @Size(min = 2, max = 100, message = "Name must be 2-100 chars")
    private String deliveryName;

    @NotBlank(message = "Phone is required")
    @Size(min = 7, max = 30, message = "Phone must be 7-30 chars")
    @Pattern(
            regexp = "^[+0-9()\\-\\s]{7,30}$",
            message = "Phone contains invalid characters"
    )
    private String deliveryPhone;

    @NotBlank(message = "Address is required")
    @Size(min = 10, max = 300, message = "Address must be 10-300 chars")
    private String deliveryAddress;

    @Size(max = 500, message = "Comment must be <= 500 chars")
    private String deliveryComment;

    @DecimalMin(value = "0.01", message = "Top up must be >= 0.01")
    @Digits(integer = 10, fraction = 2, message = "Invalid amount format")
    private BigDecimal topUpAmount;
}
