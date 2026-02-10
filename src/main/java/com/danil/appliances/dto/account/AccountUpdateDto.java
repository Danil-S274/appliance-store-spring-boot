package com.danil.appliances.dto.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AccountUpdateDto {

    @NotBlank
    @Size(min = 3, max = 100)
    private String name;
}