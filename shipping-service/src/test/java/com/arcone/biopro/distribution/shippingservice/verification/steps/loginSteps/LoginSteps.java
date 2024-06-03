package com.arcone.biopro.distribution.shippingservice.verification.steps.loginSteps;

import com.arcone.biopro.distribution.shippingservice.verification.pages.distribution.HomePage;
import io.cucumber.java.en.Given;
import org.springframework.beans.factory.annotation.Autowired;

public class LoginSteps {

    @Autowired
    private HomePage homePage;

    @Given("I have successfully logged into Distribution module.")
    public void iHaveLoggedIntoDistribution() throws InterruptedException {
        homePage.goTo();
    }
}
