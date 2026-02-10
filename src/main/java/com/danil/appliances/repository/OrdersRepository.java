package com.danil.appliances.repository;

import com.danil.appliances.model.OrderStatus;
import com.danil.appliances.model.Orders;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrdersRepository extends JpaRepository<Orders, Long> {

    @EntityGraph(attributePaths = {"client", "employee"})
    List<Orders> findAll();

    List<Orders> findByClientId(Long clientId);

    Optional<Orders> findByClientIdAndOrderStatus(Long clientId, OrderStatus orderStatus);

    @Query("select o from Orders o left join fetch o.rows r left join fetch r.appliance where o.id = :id")
    Optional<Orders> findWithRowsById(Long id);

    boolean existsByClientIdAndOrderStatus(Long clientId, OrderStatus orderStatus);
}

