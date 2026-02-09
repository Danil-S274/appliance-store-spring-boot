package com.danil.appliances.mapper;

import com.danil.appliances.dto.ApplianceDto;
import com.danil.appliances.model.Appliance;
import com.danil.appliances.model.Manufacturer;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-09T13:36:21+0000",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class ApplianceMapperImpl implements ApplianceMapper {

    @Override
    public ApplianceDto toDto(Appliance entity) {
        if ( entity == null ) {
            return null;
        }

        ApplianceDto applianceDto = new ApplianceDto();

        applianceDto.setManufacturerId( entityManufacturerId( entity ) );
        applianceDto.setManufacturerName( entityManufacturerName( entity ) );
        applianceDto.setId( entity.getId() );
        applianceDto.setName( entity.getName() );
        applianceDto.setCategory( entity.getCategory() );
        applianceDto.setModel( entity.getModel() );
        applianceDto.setPowerType( entity.getPowerType() );
        applianceDto.setCharacteristic( entity.getCharacteristic() );
        applianceDto.setDescription( entity.getDescription() );
        applianceDto.setPower( entity.getPower() );
        applianceDto.setPrice( entity.getPrice() );

        return applianceDto;
    }

    @Override
    public Appliance toEntity(ApplianceDto dto) {
        if ( dto == null ) {
            return null;
        }

        Appliance appliance = new Appliance();

        appliance.setId( dto.getId() );
        appliance.setName( dto.getName() );
        appliance.setCategory( dto.getCategory() );
        appliance.setModel( dto.getModel() );
        appliance.setPowerType( dto.getPowerType() );
        appliance.setCharacteristic( dto.getCharacteristic() );
        appliance.setDescription( dto.getDescription() );
        appliance.setPower( dto.getPower() );
        appliance.setPrice( dto.getPrice() );

        return appliance;
    }

    @Override
    public void updateEntity(ApplianceDto dto, Appliance entity) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getId() != null ) {
            entity.setId( dto.getId() );
        }
        if ( dto.getName() != null ) {
            entity.setName( dto.getName() );
        }
        if ( dto.getCategory() != null ) {
            entity.setCategory( dto.getCategory() );
        }
        if ( dto.getModel() != null ) {
            entity.setModel( dto.getModel() );
        }
        if ( dto.getPowerType() != null ) {
            entity.setPowerType( dto.getPowerType() );
        }
        if ( dto.getCharacteristic() != null ) {
            entity.setCharacteristic( dto.getCharacteristic() );
        }
        if ( dto.getDescription() != null ) {
            entity.setDescription( dto.getDescription() );
        }
        if ( dto.getPower() != null ) {
            entity.setPower( dto.getPower() );
        }
        if ( dto.getPrice() != null ) {
            entity.setPrice( dto.getPrice() );
        }
    }

    private Long entityManufacturerId(Appliance appliance) {
        if ( appliance == null ) {
            return null;
        }
        Manufacturer manufacturer = appliance.getManufacturer();
        if ( manufacturer == null ) {
            return null;
        }
        Long id = manufacturer.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String entityManufacturerName(Appliance appliance) {
        if ( appliance == null ) {
            return null;
        }
        Manufacturer manufacturer = appliance.getManufacturer();
        if ( manufacturer == null ) {
            return null;
        }
        String name = manufacturer.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }
}
