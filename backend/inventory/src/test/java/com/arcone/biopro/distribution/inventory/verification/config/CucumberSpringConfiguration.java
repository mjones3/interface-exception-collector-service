package com.arcone.biopro.distribution.inventory.verification.config;

import com.arcone.biopro.distribution.inventory.BioProApplication;
import com.arcone.biopro.distribution.inventory.domain.event.InventoryEventPublisher;
import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@CucumberContextConfiguration
@SpringBootTest(classes = BioProApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1,
    brokerProperties = {
        "listeners=PLAINTEXT://localhost:9092",
        "port=9092",
        // allow up to 500 MB messages
        "socket.request.max.bytes=524288000",
        "message.max.bytes=524288000",
        "replica.fetch.max.bytes=524288000"
    })
public class CucumberSpringConfiguration {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @BeforeEach
    public void setUp() {
    }

}
