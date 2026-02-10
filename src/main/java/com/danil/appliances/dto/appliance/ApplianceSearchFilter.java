package com.danil.appliances.dto.appliance;

import com.danil.appliances.model.Category;
import com.danil.appliances.model.PowerType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ApplianceSearchFilter {

    private String q;

    private Category category;

    private Long manufacturerId;

    private PowerType powerType;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;
}
