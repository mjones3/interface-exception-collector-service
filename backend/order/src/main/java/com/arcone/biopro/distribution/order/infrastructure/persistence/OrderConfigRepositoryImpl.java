package com.arcone.biopro.distribution.order.infrastructure.persistence;

import com.arcone.biopro.distribution.order.domain.repository.OrderConfigRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.by;
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

    private Flux<LookupEntity> findAllByType(final String type) {
        return this.entityTemplate
            .select(LookupEntity.class)
            .matching(
                query(
                    where("active").isTrue()
                        .and("type").is(type)
                )
                    .sort(by(ASC, "order_number"))
            )
            .all();
    }

    @Override
    public Mono<Boolean> findFirstConfigAsBoolean(String type) {
        return this.findAllByType(type)
            .next()
            .map(lookup -> BooleanUtils.toBoolean(lookup.getOptionValue()));
    }
}
