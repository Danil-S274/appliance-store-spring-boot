package com.danil.appliances.mapper;

import com.danil.appliances.dto.ClientCreateDto;
import com.danil.appliances.dto.ClientDto;
import com.danil.appliances.dto.ClientUpdateDto;
import com.danil.appliances.model.Client;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-09T13:36:21+0000",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class ClientMapperImpl implements ClientMapper {

    @Override
    public Client toEntity(ClientCreateDto dto) {
        if ( dto == null ) {
            return null;
        }

        Client client = new Client();

        client.setName( dto.getName() );
        client.setEmail( dto.getEmail() );

        return client;
    }

    @Override
    public void updateEntity(ClientUpdateDto dto, Client entity) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getName() != null ) {
            entity.setName( dto.getName() );
        }
    }

    @Override
    public ClientDto toDto(Client entity) {
        if ( entity == null ) {
            return null;
        }

        ClientDto clientDto = new ClientDto();

        clientDto.setId( entity.getId() );
        clientDto.setName( entity.getName() );
        clientDto.setEmail( entity.getEmail() );
        clientDto.setEnabled( entity.isEnabled() );
        clientDto.setCardLast4( entity.getCardLast4() );
        clientDto.setBalance( entity.getBalance() );

        return clientDto;
    }
}
