package com.arcone.biopro.distribution.shipping.infrastructure.persistence;

import com.arcone.biopro.distribution.shipping.domain.model.Reason;
import com.arcone.biopro.distribution.shipping.domain.repository.ReasonRepository;
import com.arcone.biopro.distribution.shipping.infrastructure.mapper.ReasonMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.by;
import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@Repository
@RequiredArgsConstructor
public class ReasonRepositoryImpl implements ReasonRepository {

    private final R2dbcEntityTemplate entityTemplate;
    private final ReasonMapper reasonMapper;

    @Override
    public Flux<Reason> findAllByType(final String type) {
        return this.entityTemplate
            .select(ReasonEntity.class)
            .matching(
                query(
                    where("active").isTrue()
                        .and("type").is(type)
                )
                    .sort(by(ASC, "order_number"))
            )
            .all()
            .flatMap(reasonMapper::flatMapToDomain);
    }
}
