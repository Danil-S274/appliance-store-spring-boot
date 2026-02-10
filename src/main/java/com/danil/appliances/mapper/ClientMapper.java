package com.danil.appliances.mapper;

import com.danil.appliances.config.MapstructConfig;
import com.danil.appliances.dto.user.ClientCreateDto;
import com.danil.appliances.dto.user.ClientDto;
import com.danil.appliances.dto.user.ClientUpdateDto;
import com.danil.appliances.model.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapstructConfig.class)
public interface ClientMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "balance", ignore = true)
    @Mapping(target = "cardHash", ignore = true)
    @Mapping(target = "cardLast4", ignore = true)
    @Mapping(target = "provider", ignore = true)
    Client toEntity(ClientCreateDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "balance", ignore = true)
    @Mapping(target = "cardHash", ignore = true)
    @Mapping(target = "cardLast4", ignore = true)
    @Mapping(target = "provider", ignore = true)
    void updateEntity(ClientUpdateDto dto, @MappingTarget Client entity);

    ClientDto toDto(Client entity);
}

