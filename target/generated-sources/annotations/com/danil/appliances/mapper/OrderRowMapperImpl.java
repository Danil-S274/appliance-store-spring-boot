package com.danil.appliances.mapper;

import com.danil.appliances.dto.OrderRowDto;
import com.danil.appliances.model.Appliance;
import com.danil.appliances.model.OrderRow;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-09T13:36:21+0000",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class OrderRowMapperImpl implements OrderRowMapper {

    @Override
    public OrderRowDto toDto(OrderRow entity) {
        if ( entity == null ) {
            return null;
        }

        OrderRowDto.OrderRowDtoBuilder orderRowDto = OrderRowDto.builder();

        orderRowDto.applianceName( entityApplianceName( entity ) );
        if ( entity.getNumber() != null ) {
            orderRowDto.number( entity.getNumber() );
        }
        orderRowDto.id( entity.getId() );
        orderRowDto.amount( entity.getAmount() );

        return orderRowDto.build();
    }

    private String entityApplianceName(OrderRow orderRow) {
        if ( orderRow == null ) {
            return null;
        }
        Appliance appliance = orderRow.getAppliance();
        if ( appliance == null ) {
            return null;
        }
        String name = appliance.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }
}
