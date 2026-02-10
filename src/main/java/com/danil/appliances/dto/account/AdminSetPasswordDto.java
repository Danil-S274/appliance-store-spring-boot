package com.danil.appliances.dto.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminSetPasswordDto {

    @Size(min = 8, max = 200, message = "Password must be at least 8 characters")
    @NotBlank(message = "Password is required")
    private String password;
}
