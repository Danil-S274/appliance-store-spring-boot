package com.danil.appliances.service;

import com.danil.appliances.dto.account.RegisterDto;
import com.danil.appliances.model.Client;

public interface AuthService {
    Client registerClient(RegisterDto dto);
}
