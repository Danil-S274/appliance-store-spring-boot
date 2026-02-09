package com.danil.appliances.repository;

import com.danil.appliances.model.OrderRow;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRowRepository extends JpaRepository<OrderRow, Long> {

    @EntityGraph(attributePaths = {"appliance"})
    List<OrderRow> findByOrdersId(Long orderId);

    Optional<OrderRow> findByIdAndOrdersId(Long rowId, Long ordersId);

    Optional<OrderRow> findByOrdersIdAndApplianceId(Long ordersId, Long applianceId);
}

