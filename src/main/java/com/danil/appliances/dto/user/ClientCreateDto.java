package com.danil.appliances.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClientCreateDto {

    @NotBlank
    @Size(min = 3, max = 100)
    private String name;

    @NotBlank
    @Email
    @Size(max = 150)
    private String email;

    @NotBlank
    @Size(min = 8, max = 200)
    private String password;

    @NotBlank
    @Pattern(
            regexp = "^[0-9]{13,19}$",
            message = "Card number must be 13-19 digits"
    )
    private String cardNumber;
}
