package com.danil.appliances.service;

import com.danil.appliances.dto.account.CheckoutRequestDto;
import com.danil.appliances.dto.orders.OrderCreateDto;
import com.danil.appliances.model.Orders;
import org.springframework.security.core.Authentication;

public interface OrderCommandService {


    Orders createForEmployee(OrderCreateDto dto);

    Orders getOrCreateDraft(String clientEmail);

    void addItemToDraft(String clientEmail, Long applianceId, long quantity);

    void updateItem(String clientEmail, Long rowId, long quantity);

    void updateDraftDelivery(String clientEmail, CheckoutRequestDto dto);

    void deleteItem(String clientEmail, Long rowId);

    Orders checkout(String clientEmail);

    Orders approve(Long orderId, Authentication auth);

    Orders cancel(Long orderId, Authentication auth);

    void deleteOrder(Long orderId, Authentication auth);
}
