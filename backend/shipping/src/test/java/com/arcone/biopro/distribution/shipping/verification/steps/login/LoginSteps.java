package com.arcone.biopro.distribution.shipping.verification.steps.login;

import com.arcone.biopro.distribution.shipping.verification.pages.distribution.HomePage;
import io.cucumber.java.en.Given;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
public class LoginSteps {

    @Autowired
    private HomePage homePage;

    @Given("I have successfully logged into Distribution module.")
    public void iHaveLoggedIntoDistribution() throws InterruptedException {
        homePage.goTo();
        homePage.isLoaded();
    }
}
