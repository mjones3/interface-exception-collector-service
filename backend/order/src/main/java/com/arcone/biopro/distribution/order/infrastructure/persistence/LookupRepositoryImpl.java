package com.arcone.biopro.distribution.order.infrastructure.persistence;

import com.arcone.biopro.distribution.order.domain.model.Lookup;
import com.arcone.biopro.distribution.order.domain.model.vo.LookupId;
import com.arcone.biopro.distribution.order.domain.repository.LookupRepository;
import com.arcone.biopro.distribution.order.infrastructure.mapper.LookupEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static java.lang.Boolean.TRUE;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.by;
import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@Component
@RequiredArgsConstructor
public class LookupRepositoryImpl implements LookupRepository {

    private final R2dbcEntityTemplate entityTemplate;
    private final LookupEntityMapper lookupEntityMapper;

    private Query queryByUniqueKey(final LookupId id, final Boolean active) {
        var criteria = where("type").is(id.getType())
            .and("option_value").is(id.getOptionValue());

        if (active != null) {
            criteria.and("active").is(active);
        }

        return query(criteria);
    }

    @Override
    public Mono<Boolean> existsById(final LookupId id) {
        return this.existsById(id, TRUE);
    }

    @Override
    public Mono<Boolean> existsById(final LookupId id, final Boolean active) {
        return this.entityTemplate
            .select(LookupEntity.class)
            .matching(queryByUniqueKey(id, active))
            .exists();
    }

    private Mono<LookupEntity> findOneEntityById(final LookupId id) {
        return this.entityTemplate
            .select(LookupEntity.class)
            .matching(queryByUniqueKey(id, TRUE))
            .one();
    }

    @Override
    public Mono<Lookup> findOneById(final LookupId id) {
        return findOneEntityById(id)
            .map(lookupEntityMapper::mapToDomain);
    }

    @Override
    public Flux<Lookup> findAllByType(final String type) {
        return this.entityTemplate
            .select(LookupEntity.class)
            .matching(
                query(
                    where("active").isTrue()
                        .and("type").is(type)
                )
                .sort(by(ASC, "order_number"))
            )
            .all()
            .flatMap(lookupEntityMapper::flatMapToDomain);
    }

    @Override
    public Mono<Lookup> insert(final Lookup lookup) {
        return this.entityTemplate
            .insert(
                LookupEntity.builder()
                    .type(lookup.getId().getType())
                    .optionValue(lookup.getId().getOptionValue())
                    .descriptionKey(lookup.getDescriptionKey())
                    .orderNumber(lookup.getOrderNumber())
                    .active(true)
                    .build()
            )
            .map(lookupEntityMapper::mapToDomain);
    }

    @Override
    public Mono<Lookup> update(final Lookup lookup) {
        return this.findOneEntityById(lookup.getId())
            .flatMap(lookupEntity -> this.entityTemplate
                .update(
                    lookupEntity.toBuilder()
                        .type(lookup.getId().getType())
                        .optionValue(lookup.getId().getOptionValue())
                        .descriptionKey(lookup.getDescriptionKey())
                        .orderNumber(lookup.getOrderNumber())
                        .active(lookup.isActive())
                        .build()
                )
            )
            .map(lookupEntityMapper::mapToDomain);
    }

}
