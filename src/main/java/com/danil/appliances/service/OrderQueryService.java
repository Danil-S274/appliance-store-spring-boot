package com.danil.appliances.service;

import com.danil.appliances.model.Orders;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface OrderQueryService {

    List<Orders> listForEmployee();

    List<Orders> listForClient(String clientEmail);

    Orders getDetailsForEmployee(Long orderId);

    Orders getDetailsForClient(Long orderId, String clientEmail);

    Orders getDraftForClient(String clientEmail);

    Orders getOrderForRead(Long orderId, Authentication auth);
}
