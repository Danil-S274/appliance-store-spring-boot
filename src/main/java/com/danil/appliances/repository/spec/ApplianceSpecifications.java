package com.danil.appliances.repository.spec;

import com.danil.appliances.dto.ApplianceSearchFilter;
import com.danil.appliances.model.Appliance;
import com.danil.appliances.model.Manufacturer;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

public final class ApplianceSpecifications {

    private ApplianceSpecifications() {}

    public static Specification<Appliance> byFilter(ApplianceSearchFilter f) {
        return Specification.where(textSearch(f.getQ()))
                .and(categoryEq(f))
                .and(powerTypeEq(f))
                .and(manufacturerEq(f))
                .and(minPrice(f))
                .and(maxPrice(f));
    }

    private static Specification<Appliance> textSearch(String q) {
        if (q == null || q.isBlank()) return null;
        String like = "%" + q.trim().toLowerCase(Locale.ROOT) + "%";

        return (root, query, cb) -> {
            query.distinct(true);

            return cb.or(
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(root.get("model")), like),
                    cb.like(cb.lower(root.get("description")), like),
                    cb.like(cb.lower(root.get("characteristic")), like)
            );
        };
    }

    private static Specification<Appliance> categoryEq(ApplianceSearchFilter f) {
        if (f.getCategory() == null) return null;
        return (root, query, cb) -> cb.equal(root.get("category"), f.getCategory());
    }

    private static Specification<Appliance> powerTypeEq(ApplianceSearchFilter f) {
        if (f.getPowerType() == null) return null;
        return (root, query, cb) -> cb.equal(root.get("powerType"), f.getPowerType());
    }

    private static Specification<Appliance> manufacturerEq(ApplianceSearchFilter f) {
        if (f.getManufacturerId() == null) return null;

        return (root, query, cb) -> {
            Join<Appliance, Manufacturer> m = root.join("manufacturer", JoinType.INNER);
            return cb.equal(m.get("id"), f.getManufacturerId());
        };
    }

    private static Specification<Appliance> minPrice(ApplianceSearchFilter f) {
        if (f.getMinPrice() == null) return null;
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), f.getMinPrice());
    }

    private static Specification<Appliance> maxPrice(ApplianceSearchFilter f) {
        if (f.getMaxPrice() == null) return null;
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), f.getMaxPrice());
    }
}
