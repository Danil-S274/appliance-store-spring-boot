package com.danil.appliances.service;

import com.danil.appliances.dto.user.ClientCreateDto;
import com.danil.appliances.dto.user.ClientUpdateDto;
import com.danil.appliances.model.Client;

import java.util.List;

public interface ClientService {

    List<Client> findAll();

    Client findById(Long id);

    Client create(ClientCreateDto dto);

    Client update(Long id, ClientUpdateDto dto);

    void delete(Long id);

    void setEnabled(Long id, boolean enabled);
}
