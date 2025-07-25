package com.arcone.biopro.distribution.irradiation.verification.ui.pages;

import com.arcone.biopro.testing.frontend.core.PageElement;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Slf4j
@Lazy
@Component
public class StartIrradiationPage extends IrradiationPage {

    // Start Irradiation specific locators
    private final By lotNumberInputLocator = By.id("lotNumberInput");
    private final By pageTitleLocator = By.xpath("//h3//span[contains(text(),'Start Irradiation')]");

    @Override
    public boolean isLoaded() {
        try {
            PageElement pageTitle = driver.waitForElement(pageTitleLocator, 5);
            pageTitle.waitForVisible();
            return pageTitle.isDisplayed();
        } catch (org.openqa.selenium.TimeoutException e) {
            log.info("Start Irradiation page not loaded: {}", e.getMessage());
            return false;
        }
    }
    
    public void scanLotNumber(String lotNumber) {
        enterValueInField(lotNumber, lotNumberInputLocator, true);
    }
    
    @Override
    public boolean inputFieldIsEnabled(String input) {
        if ("Lot Number".equals(input)) {
            PageElement field = driver.findElement(lotNumberInputLocator);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return field.isEnabled();
        }
        return super.inputFieldIsEnabled(input);
    }
}
