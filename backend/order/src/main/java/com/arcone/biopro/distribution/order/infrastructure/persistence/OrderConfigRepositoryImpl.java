package com.arcone.biopro.distribution.order.infrastructure.persistence;

import com.arcone.biopro.distribution.order.domain.repository.OrderConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@Component
@RequiredArgsConstructor
public class OrderConfigRepositoryImpl implements OrderConfigRepository {

    private final R2dbcEntityTemplate entityTemplate;


    @Override
    public Mono<String> findProductFamilyByCategory(String productCategory, String productFamily) {

        var criteria = where("family_category").is(productCategory)
            .and("product_family").is(productFamily);
            criteria.and("active").is(Boolean.TRUE);
        var query = query(criteria);

        return this.entityTemplate
            .select(OrderProductFamilyEntity.class)
            .matching(query)
            .one()
            .map(OrderProductFamilyEntity::getProductFamily);
    }

    @Override
    public Mono<String> findBloodTypeByFamilyAndType(String productFamily, String bloodType) {
        var criteria = where("product_family").is(productFamily)
            .and("blood_type").is(bloodType);
        criteria.and("active").is(Boolean.TRUE);
        var query = query(criteria);

        return this.entityTemplate
            .select(OrderBloodTypeEntity.class)
            .matching(query)
            .one()
            .map(OrderBloodTypeEntity::getBloodType);
    }
}
