package com.arcone.biopro.distribution.partnerorderprovider.verification.steps;

import com.arcone.biopro.distribution.partnerorderprovider.verification.support.SharedContext;
import com.arcone.biopro.distribution.partnerorderprovider.verification.support.TestUtils;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.json.JSONException;
import org.junit.Assert;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SharedSteps {

    @Autowired
    private SharedContext context;

    @Autowired
    private TestUtils testUtils;

    @Then("The response status code should be {int}.")
    public void validateResponse(int responseCode){
        Assert.assertEquals(responseCode, context.getApiResponseCode());
    }

    @And("The response status should be {string}.")
    public void checkResponseStatus(String status){
        var statusResponse  = HttpStatus.valueOf(status);
        Assert.assertEquals(statusResponse, context.getApiResponseStatus());
    }

    @And("The error message should be {string}.")
    public void checkErrorMessage(String message) throws JSONException {
        testUtils.checkErrorMessage(message,context.getApiMessageResponseBody());
    }
}
