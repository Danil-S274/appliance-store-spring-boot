package com.danil.appliances.dto.appliance;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ManufacturerDto {

    private Long id;

    @NotBlank
    @Size(max = 150)
    private String name;
}
