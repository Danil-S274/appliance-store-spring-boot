package com.danil.appliances.mapper;

import com.danil.appliances.dto.ManufacturerDto;
import com.danil.appliances.model.Manufacturer;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-09T13:36:21+0000",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class ManufacturerMapperImpl implements ManufacturerMapper {

    @Override
    public ManufacturerDto toDto(Manufacturer entity) {
        if ( entity == null ) {
            return null;
        }

        ManufacturerDto manufacturerDto = new ManufacturerDto();

        manufacturerDto.setId( entity.getId() );
        manufacturerDto.setName( entity.getName() );

        return manufacturerDto;
    }

    @Override
    public Manufacturer toEntity(ManufacturerDto dto) {
        if ( dto == null ) {
            return null;
        }

        Manufacturer manufacturer = new Manufacturer();

        manufacturer.setId( dto.getId() );
        manufacturer.setName( dto.getName() );

        return manufacturer;
    }

    @Override
    public void updateEntity(ManufacturerDto dto, Manufacturer entity) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getId() != null ) {
            entity.setId( dto.getId() );
        }
        if ( dto.getName() != null ) {
            entity.setName( dto.getName() );
        }
    }
}
