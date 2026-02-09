package com.danil.appliances.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClientUpdateDto {

    @NotBlank
    @Size(min = 3, max = 100)
    private String name;
}
