package com.arcone.biopro.distribution.shipping.verification.support;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class DatabaseService {
    private final DatabaseClient databaseClient;

    public Mono<Long> executeSql(String sql) {
        log.info("Running SQL {}", sql);
        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql);
        return spec.fetch().rowsUpdated();
    }
}
