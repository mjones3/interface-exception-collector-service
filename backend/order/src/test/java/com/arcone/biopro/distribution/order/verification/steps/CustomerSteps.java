package com.arcone.biopro.distribution.order.verification.steps;

import com.arcone.biopro.distribution.order.verification.controllers.CustomerController;
import com.arcone.biopro.distribution.order.verification.support.ApiHelper;
import com.arcone.biopro.distribution.order.verification.support.GraphQLQueryMapper;
import com.arcone.biopro.distribution.order.verification.support.types.CustomerType;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CustomerSteps {

    @Autowired
    private ApiHelper apiHelper;

    private String code;
    private CustomerType customer;
    private Map response;

    @Given("I have a customer which code is {string}.")
    public void defineCustomerCode(String code) {
        this.code = code;
    }

    @When("I search for the customer by code.")
    public void searchCustomer() {
        this.response = apiHelper.graphQlRequest(GraphQLQueryMapper.findCustomerByCode(code), "findCustomerByCode");
        if (response.get("errors") == null) {
            this.customer = CustomerController.parseCustomerByCodeResponse(response);
        }
    }

    @And("I should see the customer name {string}.")
    public void checkCustomerName(String name) {
        Assert.assertEquals(name, customer.name());
    }

    @And("I should see the customer active flag as {string}.")
    public void checkCustomerActiveFlag(String active) {
        Assert.assertEquals(active, customer.active());
    }

    @And("I should have an address of type {string}.")
    public void checkCustomerAddressType(String addressType) {
        customer.addresses().stream()
            .filter(a -> a.addressType().equals(addressType))
            .findFirst()
            .ifPresent(Assert::assertNotNull);
    }

    @And("The attributes {string} are not empty.")
    public void checkCustomerAttributes(String attributes) {
        Arrays.stream(attributes.split(",")).map(String::trim).forEach
            (attribute -> {
                switch (attribute) {
                    case "externalId":
                        Assert.assertNotNull(customer.externalId());
                        break;
                    case "name":
                        Assert.assertNotNull(customer.name());
                        break;
                    case "code":
                        Assert.assertNotNull(customer.code());
                        break;
                    case "departmentCode":
                        Assert.assertNotNull(customer.departmentCode());
                        break;
                    case "departmentName":
                        Assert.assertNotNull(customer.departmentName());
                        break;
                    case "phoneNumber":
                        Assert.assertNotNull(customer.phoneNumber());
                        break;
                    case "active":
                        Assert.assertNotNull(customer.active());
                        break;
                    case "addresses":
                        Assert.assertNotNull(customer.addresses());
                        break;
                    default:
                        Assert.fail("Attribute not found: " + attribute);
                }
            });
    }

    @Then("I should see a message {string}.")
    public void checkErrorMessage(String message) {
        var errors = (List<Map<String, ?>>) response.get("errors");
        var expectedErrorMessage = errors.stream()
            .map(e -> (String) e.get("message"))
            .filter(m -> m.equals(message))
            .findAny()
            .orElse(null);

        Assert.assertNotNull(expectedErrorMessage);
    }
}
