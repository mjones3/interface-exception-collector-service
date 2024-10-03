package com.arcone.biopro.distribution.shipping.infrastructure.persistence;

import com.arcone.biopro.distribution.shipping.domain.repository.LookupRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static java.lang.Boolean.TRUE;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.by;
import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@Component
@RequiredArgsConstructor
public class LookupRepositoryImpl implements LookupRepository {

    private final R2dbcEntityTemplate entityTemplate;

    private Query queryByUniqueKey(String type, String optionValue, Boolean active) {
        var criteria = where("type").is(type)
            .and("option_value").is(optionValue);

        if (active != null) {
            criteria.and("active").is(active);
        }

        return query(criteria);
    }

    private Mono<Boolean> existsById(String type, String optionValue, final Boolean active) {
        return this.entityTemplate
            .select(LookupEntity.class)
            .matching(queryByUniqueKey(type, optionValue, active))
            .exists();
    }

    private Mono<LookupEntity> findOneEntityById(String type, String optionValue) {
        return this.entityTemplate
            .select(LookupEntity.class)
            .matching(queryByUniqueKey(type, optionValue, TRUE))
            .one();
    }

    private Mono<LookupEntity> findOneById(String type, String optionValue) {
        return findOneEntityById(type, optionValue);
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
    public Mono<String> findFirstConfigAsString(String type) {
        return this.findAllByType(type)
            .next()
            .map(LookupEntity::getOptionValue);
    }

    @Override
    public Mono<Boolean> findFirstConfigAsBoolean(String type) {
        return this.findFirstConfigAsString(type)
            .map(BooleanUtils::toBooleanObject);
    }

    @Override
    public Mono<Integer> findFirstConfigAsInteger(String type) {
        return this.findFirstConfigAsString(type)
            .map(Integer::valueOf);
    }

    @Override
    public Mono<Long> findFirstConfigAsLong(String type) {
        return this.findFirstConfigAsString(type)
            .map(Long::valueOf);
    }

    @Override
    public Mono<BigDecimal> findFirstConfigAsBigDecimal(String type) {
        return this.findFirstConfigAsString(type)
            .map(BigDecimal::new);
    }

    @Override
    public Flux<String> findAllConfigsAsStrings(String type) {
        return this.findAllByType(type)
            .map(LookupEntity::getOptionValue);
    }

    @Override
    public Flux<Integer> findAllConfigsAsIntegers(String type) {
        return this.findAllConfigsAsStrings(type)
            .map(Integer::valueOf);
    }

    @Override
    public Flux<Long> findAllConfigsAsLongs(String type) {
        return this.findAllConfigsAsStrings(type)
            .map(Long::valueOf);
    }

    @Override
    public Flux<BigDecimal> findAllConfigsAsBigDecimals(String type) {
        return this.findAllConfigsAsStrings(type)
            .map(BigDecimal::new);
    }

}
