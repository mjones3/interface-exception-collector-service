package com.arcone.biopro.distribution.customer.verification.config;

import com.arcone.biopro.distribution.customer.verification.support.DatabaseQueries;
import com.arcone.biopro.distribution.customer.verification.support.DatabaseService;
import io.cucumber.java.After;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import reactor.core.publisher.Flux;

import java.util.List;

@SpringBootTest
@Slf4j
public class CucumberHooks {

    @Autowired
    private DatabaseService databaseService;

    @Autowired
    public ApplicationContext ctx;

    @After("@cleanup")
    public void cleanupAfterScenario() {
        List<String> queries = DatabaseQueries.deleteCustomersByTestPrefixQueries();

        Flux.fromIterable(queries)
                .concatMap(query -> databaseService.executeSql(query)
                        .doOnNext(rowsAffected -> log.info("Query executed {} rows for query: {}", rowsAffected)))
                .collectList()
                .block();

        log.info("Successfully completed cleanup of test donors and related data");
    }

    @After(value = "@ui")
    public void afterScenario() {
        this.ctx.getBean(WebDriver.class).quit();
    }

}
