package com.arcone.biopro.distribution.irradiation.verification.ui.steps;

import com.arcone.biopro.distribution.irradiation.verification.ui.pages.HomePage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.AllArgsConstructor;
import org.openqa.selenium.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

@AllArgsConstructor
public class NavigationSteps {

    private static final Logger log = LoggerFactory.getLogger(NavigationSteps.class);
    private final HomePage homePage;

    @Given("I login to Manufacturing module")
    public void iNavigateToManufacturingModule() {
        homePage.goTo();
    }

    @When("I select the location {string}")
    public void iSelectLocation(String location) {
        homePage.selectLocation(location);
    }

    @And("I navigate to {string}")
    public void iNavigateTo(String menuItem) throws Exception {
        homePage.goToProcess(menuItem);
    }

    @Then("I verify that I am taken to the page {string} in {string}")
    public void verifyiAmtakenToThePage(String page, String module) {
        try {
            homePage.waitForPageToLoad(module, page, 10);
        } catch (TimeoutException e) {
            fail("The expected page did not load within 10 seconds. Expected module: '"
                + module + "', page: '" + page + "'.");
        }
        String actualTitle = homePage.getPageTitle();
        String actualSubTitle = homePage.getPageSubTitle();

        assertEquals(module, actualTitle, "The page title is not equal to: " + module);
        assertEquals(page, actualSubTitle, "The page sub-title is not equal to: " + page);
    }

}
