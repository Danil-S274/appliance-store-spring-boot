package com.danil.appliances.dto.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateCardDto {

    @NotBlank
    @Pattern(regexp = "^[0-9 ]{13,23}$", message = "Card number must be 13-19 digits (spaces allowed)")
    private String cardNumber;
}
