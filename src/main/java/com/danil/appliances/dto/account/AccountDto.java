package com.danil.appliances.dto.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AccountDto {

    @NotBlank
    @Size(min = 3, max = 100)
    private String name;

    @NotBlank
    @Size(min = 4, max = 50)
    @Pattern(regexp = "^[\\w\\-\\s]+$", message = "Invalid card format")
    private String card;
}
