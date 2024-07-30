package com.arcone.biopro.distribution.order.verification.support;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.FetchSpec;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

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

    public FetchSpec<Map<String, Object>> fetchData(String sql) {
        log.info("Running SQL {}", sql);
        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql);
        return spec.fetch();
    }
}
