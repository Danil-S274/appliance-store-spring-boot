package com.danil.appliances.service;

import com.danil.appliances.dto.AccountUpdateDto;
import com.danil.appliances.dto.ChangePasswordDto;
import com.danil.appliances.dto.UpdateCardDto;
import com.danil.appliances.model.Client;

import java.math.BigDecimal;

public interface AccountService {

    Client getClient(String email);

    BigDecimal getBalance(String email);

    Client updateProfile(String email, AccountUpdateDto dto);

    Client updateCard(String email, UpdateCardDto dto);

    void changeClientPassword(String email, ChangePasswordDto dto);

    BigDecimal topUpBalance(String email, BigDecimal amount);

    void deleteAccount(String email);
}

