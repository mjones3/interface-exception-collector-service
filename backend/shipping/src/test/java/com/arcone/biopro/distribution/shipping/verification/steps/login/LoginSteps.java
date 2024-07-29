package com.arcone.biopro.distribution.shipping.verification.steps.login;

import com.arcone.biopro.distribution.shipping.verification.pages.distribution.HomePage;
import com.arcone.biopro.distribution.shipping.verification.support.ScreenshotService;
import io.cucumber.java.en.Given;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Lazy;

@SpringBootTest
@Slf4j
public class LoginSteps {

    @Autowired
    private HomePage homePage;

    @Lazy
    @Autowired
    private ScreenshotService screenshot;

    @Value("${save.all.screenshots}")
    private boolean saveAllScreenshots;

    @Given("I have successfully logged into Distribution module.")
    public void iHaveLoggedIntoDistribution() throws InterruptedException {
        homePage.goTo();
        homePage.isLoaded();
        screenshot.attachConditionalScreenshot(saveAllScreenshots);
    }
}
