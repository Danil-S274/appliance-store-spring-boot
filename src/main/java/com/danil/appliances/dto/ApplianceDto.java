package com.danil.appliances.dto;

import com.danil.appliances.model.Category;
import com.danil.appliances.model.PowerType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ApplianceDto {

    private Long id;

    @NotBlank
    @Size(max = 150)
    private String name;

    @NotNull
    private Category category;

    @NotBlank
    @Size(max = 150)
    private String model;

    @NotNull
    @Positive
    private Long manufacturerId;

    private String manufacturerName;

    @NotNull
    private PowerType powerType;

    @NotBlank
    @Size(max = 255)
    private String characteristic;

    @NotBlank
    @Size(max = 500)
    private String description;

    @NotNull
    @Positive
    @Max(20000)
    private Integer power;


    @NotNull
    @DecimalMin(value = "0.01")
    @Digits(integer = 12, fraction = 2)
    private BigDecimal price;
}
