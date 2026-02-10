package com.danil.appliances.mapper;

import com.danil.appliances.config.MapstructConfig;
import com.danil.appliances.dto.orders.OrderRowDto;
import com.danil.appliances.model.OrderRow;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapstructConfig.class)
public interface OrderRowMapper {

    @Mapping(target = "applianceName", source = "appliance.name")
    @Mapping(target = "number", source = "number")
    OrderRowDto toDto(OrderRow entity);
}
