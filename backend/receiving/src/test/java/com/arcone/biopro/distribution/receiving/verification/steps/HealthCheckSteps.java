package com.arcone.biopro.distribution.receiving.verification.steps;

import com.arcone.biopro.distribution.receiving.verification.support.ApiHelper;
import com.arcone.biopro.distribution.receiving.verification.support.Endpoints;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.reactive.server.EntityExchangeResult;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HealthCheckSteps {

    @Autowired
    private ApiHelper apiHelper;

    private EntityExchangeResult<String> result;

    @Given("the application is started")
    public void the_application_is_started() {
        // This step is conceptual and ensures the application context is loaded and running.
        // No action needed here as `@SpringBootTest` handles application start.
        System.out.println("Verified the application is started.");
    }

    @When("I check the health endpoint")
    public void i_check_the_health_endpoint() throws InterruptedException {
        // Using WebTestClient to hit the health check endpoint.
        result = apiHelper.getRequest(Endpoints.CHECK_HEALTH, null);
    }

    @Then("the response status should be 200")
    public void the_response_status_should_be_200() {
        // Asserting that the response from the health endpoint is 200 OK.
        assertEquals(200, result.getStatus().value(), "Health check response status is not 200.");
    }


}
