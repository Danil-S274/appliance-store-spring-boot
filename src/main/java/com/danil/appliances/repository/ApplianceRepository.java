package com.danil.appliances.repository;

import com.danil.appliances.model.Appliance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ApplianceRepository extends JpaRepository<Appliance, Long>, JpaSpecificationExecutor<Appliance> {

    @EntityGraph(attributePaths = {"manufacturer"})
    Page<Appliance> findAll(Specification<Appliance> spec, Pageable pageable);

    boolean existsByManufacturerId(Long manufacturerId);
}
