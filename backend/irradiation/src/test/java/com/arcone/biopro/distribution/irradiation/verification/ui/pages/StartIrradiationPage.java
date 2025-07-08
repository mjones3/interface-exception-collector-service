package com.arcone.biopro.distribution.irradiation.verification.ui.pages;

import com.arcone.biopro.testing.frontend.core.CommonPageFactory;
import com.arcone.biopro.testing.frontend.core.PageElement;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Slf4j
@Lazy
@Component
public class StartIrradiationPage extends CommonPageFactory {

    // locators
    private final By irradiationDeviceIdInputLocator = By.id("irradiationIdInput");
    private final By lotNumberInputLocator = By.id("lotNumberInput");
    private final By unitNumberInputLocator = By.id("inUnitNumber");
    private final By pageTitleLocator = By.xpath("//h3//span[contains(text(),'Start Irradiation')]");

    @Override
    public boolean isLoaded() {
        try {
            PageElement pageTitle = driver.waitForElement(pageTitleLocator, 5);
            pageTitle.waitForVisible();
            return pageTitle.isDisplayed();
        } catch (org.openqa.selenium.TimeoutException e) {
            log.info("Home page not loaded: {}", e.getMessage());
            return false;
        }
    }

    public void scanUnitNumber(String unitNumber) {
        enterValueInField(unitNumber,unitNumberInputLocator);
    }

    public void scanLotNumber(String lotNumber) {
        enterValueInField(lotNumber,lotNumberInputLocator);
    }

    public void scanIrradiatorDeviceId(String irradiatorDeviceId) {
        enterValueInField(irradiatorDeviceId,irradiationDeviceIdInputLocator);
    }

    private void enterValueInField(String value, By fieldLocator) {
        PageElement inputField = driver.waitForElement(fieldLocator);
        inputField.waitForVisible();
        inputField.waitForClickable();
        inputField.sendKeys(value);
        inputField.sendKeys(Keys.ENTER);
    }

    public boolean inputFieldIsEnabled(String input) {
        PageElement field = switch (input) {
            case "Unit Number" -> driver.findElement(unitNumberInputLocator);
            case "Irradiator Id" -> driver.findElement(irradiationDeviceIdInputLocator);
            case "Lot Number" -> driver.findElement(lotNumberInputLocator);
            default -> null;
        };
        assert field != null;
        return field.isEnabled();
    }
}
