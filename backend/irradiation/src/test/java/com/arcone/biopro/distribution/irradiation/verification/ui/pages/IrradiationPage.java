package com.arcone.biopro.distribution.irradiation.verification.ui.pages;

import com.arcone.biopro.common.utils.Retry;
import com.arcone.biopro.testing.frontend.core.CommonPageFactory;
import com.arcone.biopro.testing.frontend.core.PageElement;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.Keys;

import java.util.List;

import static org.junit.Assert.fail;

@Slf4j
public abstract class IrradiationPage extends CommonPageFactory {

    // Common locators
    protected final By irradiationDeviceIdInputLocator = By.id("irradiationIdInput");
    protected final By unitNumberInputLocator = By.id("inUnitNumber");

    protected By unitNumberCardLocator(String unitNumber, String product) {
        String xpathExpression = String.format("//biopro-unit-number-card//div[contains(text(),'%s')]//following-sibling::div//span[contains(text(),'%s')]", unitNumber, product);
        return By.xpath(xpathExpression);
    }

    protected By productForSelectionLocator(String productCode) {
        String xpathExpression = String.format("//button//span[contains(text(),'%s')]", productCode);
        return By.xpath(xpathExpression);
    }

    public void scanUnitNumber(String unitNumber) {
        enterValueInField(unitNumber, unitNumberInputLocator, false);
    }

    public void scanIrradiatorDeviceId(String irradiatorDeviceId) {
        enterValueInField(irradiatorDeviceId, irradiationDeviceIdInputLocator, true);
    }

    protected void enterValueInField(String value, By fieldLocator, boolean pressEnter) {
        PageElement inputField = driver.waitForElement(fieldLocator);
        inputField.waitForVisible();
        inputField.waitForClickable();
        inputField.sendKeys(value);
        if(pressEnter) {
            inputField.sendKeys(Keys.ENTER);
        }
    }

    public boolean inputFieldIsEnabled(String input) {
        PageElement field = switch (input) {
            case "Unit Number" -> driver.findElement(unitNumberInputLocator);
            case "Irradiator Id" -> driver.findElement(irradiationDeviceIdInputLocator);
            default -> null;
        };
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assert field != null;
        return field.isEnabled();
    }

    public boolean productIsDisplayedForSelection(String productCode) {
        PageElement productForSelection = driver.waitForElement(productForSelectionLocator(productCode));
        productForSelection.waitForVisible();
        return productForSelection.isDisplayed();
    }

    public void selectProductForIrradiation(String productCode) {
        int maxRetries = 3;
        int attempts = 0;
        boolean clicked = false;
        while (attempts < maxRetries && !clicked) {
            try {
                PageElement productForSelection = driver.waitForElement(productForSelectionLocator(productCode));
                productForSelection.waitForVisible();
                productForSelection.waitForClickable();
                productForSelection.click();
                clicked = true;
            } catch (ElementClickInterceptedException e) {
                attempts++;
                log.warn("Click intercepted on attempt {}. Retrying...", attempts, e);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Thread interrupted during retry delay", ie);
                }
            } catch (Exception e) {
                log.error("Unexpected error while closing pop-up", e);
                throw e;
            }
        }
        if(!clicked) {
            fail("Unable to click on product " + productCode);
        }
    }

    public boolean unitNumberCardExists(String unitNumber, String product) {
        try {
            PageElement unitNumberCard = driver.waitForElement(unitNumberCardLocator(unitNumber, product), 5);
            unitNumberCard.waitForVisible();
            return unitNumberCard.isDisplayed();
        } catch (Exception e) {
            log.error("The card for the unit number: '{}' was not displayed", unitNumber);
            return false;
        }
    }

    public int unitNumberProductCardCount(String unitNumber, String product) {
        List<PageElement> unitNumberCards = driver.findElements(unitNumberCardLocator(unitNumber, product));
        return unitNumberCards.size();
    }

    public boolean isProductInStatus(String unitNumber, String product, String expectedStatus) {
        try {
            return Retry.retryUntilTrue(() -> {
                PageElement card = driver.waitForElement(unitNumberCardLocator(unitNumber, product));
                PageElement cardStatusElement = card.findChildElement(By.xpath(String.format("//p[@id='statusClasses' and contains(text(),'%s')]", expectedStatus)));
                cardStatusElement.waitForVisible();
                return cardStatusElement.isDisplayed();
            });
        } catch (InterruptedException e) {
            log.error("Retry interrupted while checking product card status", e);
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public void selectUnitCard(String unitNumber, String product){
        try {
            PageElement unitNumberCard = driver.waitForElement(unitNumberCardLocator(unitNumber, product), 5);
            unitNumberCard.safeClick();
        } catch (Exception e) {
            log.error("Error to select the card for the unit number '{}' and product '{}' ", unitNumber, product);
        }
    }
}
