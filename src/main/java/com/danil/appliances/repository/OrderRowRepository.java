package com.danil.appliances.repository;

import com.danil.appliances.model.OrderRow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRowRepository extends JpaRepository<OrderRow, Long> {

    Optional<OrderRow> findByIdAndOrdersId(Long rowId, Long ordersId);

    Optional<OrderRow> findByOrdersIdAndApplianceId(Long ordersId, Long applianceId);
}

