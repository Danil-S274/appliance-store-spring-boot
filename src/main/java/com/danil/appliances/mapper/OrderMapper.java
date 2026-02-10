package com.danil.appliances.mapper;

import com.danil.appliances.config.MapstructConfig;
import com.danil.appliances.dto.orders.OrderDetailsDto;
import com.danil.appliances.dto.orders.OrderListItemDto;
import com.danil.appliances.model.Orders;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapstructConfig.class, uses = {OrderRowMapper.class})
public interface OrderMapper {

    @Mapping(target = "clientName", source = "client.name")
    @Mapping(target = "employeeName", expression = "java(order.getEmployee() == null ? null : order.getEmployee().getName())")
    @Mapping(target = "orderStatus", source = "orderStatus")
    @Mapping(target = "amount", expression = "java(order.getAmount())")
    @Mapping(target = "rows", source = "rows")
    OrderDetailsDto toDetailsDto(Orders order);

    @Mapping(target = "clientName", source = "client.name")
    @Mapping(target = "employeeName", expression = "java(order.getEmployee() == null ? null : order.getEmployee().getName())")
    @Mapping(target = "orderStatus", source = "orderStatus")
    @Mapping(target = "amount", expression = "java(order.getAmount())")
    OrderListItemDto toListItemDto(Orders order);
}
