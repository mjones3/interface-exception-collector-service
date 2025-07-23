package com.arcone.biopro.distribution.customer.verification.config;

import com.arcone.biopro.distribution.customer.BioProApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

@CucumberContextConfiguration
@SpringBootTest(classes = BioProApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

public class CucumberSpringConfiguration {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @BeforeEach
    public void setUp() {
    }

}
