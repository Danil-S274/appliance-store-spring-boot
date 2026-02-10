package com.danil.appliances.dto.account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterDto {

    @NotBlank @Size(min = 3, max = 100)
    private String name;

    @NotBlank @Email @Size(max = 150)
    private String email;

    @NotBlank @Size(min = 8, max = 200)
    private String password;

    @NotBlank @Size(min = 8, max = 200)
    private String confirmPassword;

    @NotBlank
    @Pattern(regexp = "^[0-9 ]{13,23}$", message = "Card number must be 13-19 digits (spaces allowed)")
    private String cardNumber;
}


