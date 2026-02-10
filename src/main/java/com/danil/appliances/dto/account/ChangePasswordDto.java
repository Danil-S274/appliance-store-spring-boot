package com.danil.appliances.dto.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordDto {

    @NotBlank
    private String currentPassword;

    @NotBlank
    @Size(min = 8, max = 200)
    private String newPassword;

    @NotBlank
    @Size(min = 8, max = 200)
    private String confirmNewPassword;
}

