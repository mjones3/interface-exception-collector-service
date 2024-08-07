package com.arcone.biopro.distribution.order.verification.pages.order;

import com.arcone.biopro.distribution.order.verification.pages.CommonPageFactory;
import com.arcone.biopro.distribution.order.verification.pages.SharedActions;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SearchOrderPage extends CommonPageFactory {

    @Autowired
    private SharedActions sharedActions;

    @Autowired
    private HomePage homePage;

    @Value("${ui.base.url}")
    private String baseUrl;

    @FindBy(xpath = "//h3/*[text()='Search Orders']")
    private WebElement searchOrdersTitle;

    @Override
    public boolean isLoaded() {
        sharedActions.waitForVisible(searchOrdersTitle);
        return sharedActions.isElementVisible(searchOrdersTitle);
    }

    public void goTo() {
        driver.get(baseUrl + "/orders/search");
        Assert.assertTrue(isLoaded());
    }
}
