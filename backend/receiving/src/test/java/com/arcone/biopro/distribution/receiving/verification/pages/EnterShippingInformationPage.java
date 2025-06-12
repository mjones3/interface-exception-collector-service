package com.arcone.biopro.distribution.receiving.verification.pages;

import com.arcone.biopro.distribution.receiving.verification.support.SharedContext;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EnterShippingInformationPage extends CommonPageFactory {

    @Autowired
    private SharedActions sharedActions;

    @Autowired
    private HomePage homePage;

    private final By manageCartonHeader = By.xpath("//h3//span[contains(text(),'Shipment Information')]");
    private final By startTransitDateInput = By.xpath("//input[@data-testid='transit-time-start-date']");
    private final By startTransitTimeInput = By.xpath("//input[@data-testid='transit-time-start-time']");
    private final By startTransitTimeZoneInput = By.xpath("//mat-select[@data-testid='transit-time-start-zone']");

    private final By endTransitDateInput = By.xpath("//input[@data-testid='transit-time-end-date']");
    private final By endTransitTimeInput = By.xpath("//input[@data-testid='transit-time-end-time']");
    private final By endTransitTiZoneInput = By.xpath("//mat-select[@data-testid='transit-time-end-zone']");

    private final By temperatureInput = By.xpath("//input[@data-testid='temperature']");
    private final By thermometerIdInput = By.xpath("//input[@data-testid='thermometer-id']");
    private final By commentsInput = By.xpath("//textarea[@data-testid='comments']");
    private final By continueButton = By.id("importsEnterShipmentInformationContinueActionButton");

    @Autowired
    private SharedContext sharedContext;

    private By matErrorDataTestId(String name) {
        return By.xpath(String.format("//mat-error[@data-testid='%s']", name));
    }

    private By selectInputOption(String optionText) {
        return By.xpath(String.format("//mat-option//*[contains(text() , '%s')]", optionText));
    }


    private By temperatureCategoryCard(String temperatureCategory) {
        return By.xpath(String.format("//button[@data-testid='select-temperature-category-%s'] ", temperatureCategory));
    }

    @Override
    public boolean isLoaded() {
        return sharedActions.isElementVisible(manageCartonHeader);
    }

    public void navigateToEnterShippingInformation() throws InterruptedException {
        var location = sharedContext.getLocationCode();
        if (location != null) {
            homePage.goTo(location);
        } else {
            homePage.goTo();
        }

        sharedActions.navigateTo("/receiving/imports-enter-shipment-information");
    }

    public void selectTemperatureCategory(String temperatureCategory) {
        sharedActions.click(temperatureCategoryCard(temperatureCategory));
    }

    public void setComments(String comments) {
        sharedActions.sendKeys(commentsInput, comments);
    }

    public void setThermometerId(String id) {
        sharedActions.sendKeys(thermometerIdInput, id);
    }

    public void setTemperature(String temperature) throws InterruptedException {
        sharedActions.sendKeysAndEnter(driver, temperatureInput, temperature);
        Thread.sleep(200); // Wait backend to process
    }

    public void setStartTransitDate(String date) {
        sharedActions.sendKeys(startTransitDateInput, date);
    }

    public void setStartTransitTime(String time) {
        sharedActions.sendKeys(startTransitTimeInput, time);
    }

    public void selectStartTransitTimeZone(boolean visible, String timeZone) {
        if (visible) {
            sharedActions.click(startTransitTimeZoneInput);
            sharedActions.sendKeys(startTransitTimeZoneInput, timeZone);
            sharedActions.click(selectInputOption(timeZone));
        } else {
            sharedActions.isElementVisible(startTransitTimeZoneInput);
        }

    }


    public void setEndTransitDate(String date) {
        sharedActions.sendKeys(endTransitDateInput, date);
    }

    public void setEndTransitTime(String time) {
        sharedActions.sendKeys(endTransitTimeInput, time);
    }

    public void selectEndTransitTimeZone(boolean visible, String timeZone) {
        if (visible) {
            sharedActions.click(endTransitTiZoneInput);
            sharedActions.sendKeys(endTransitTiZoneInput, timeZone);
            sharedActions.click(selectInputOption(timeZone));
        } else {
            sharedActions.isElementVisible(endTransitTiZoneInput);
        }

    }

    public void setRandomFormValue(String field, boolean visible) {
        if (field.equals("Transit Start Date")) {
            setValue(visible, "06/02/2025", startTransitDateInput);
        } else if (field.equals("Transit Start Time")) {
            setValue(visible, "01:15A", startTransitTimeInput);
        } else if (field.equals("Start Time Zone")) {
            selectStartTransitTimeZone(visible, "MST");
        } else if (field.equals("Transit End Date")) {
            setValue(visible, "06/02/2025", endTransitDateInput);
        } else if (field.equals("Transit End Time")) {
            setValue(visible, "01:15P", endTransitTimeInput);
        } else if (field.equals("End Time Zone")) {
            selectEndTransitTimeZone(visible, "PT");
        } else if (field.equals("Comments")) {
            setValue(visible, "test", commentsInput);
        } else if (field.equals("Thermometer")) {
            setValue(visible, "123456", thermometerIdInput);
        }
    }

    private void setValue(boolean visible, String value, By fieldId) {
        if (visible) {
            sharedActions.sendKeys(fieldId, value);
        } else {
            sharedActions.waitForNotVisible(fieldId);
        }
    }

    public boolean isTemperatureFieldEnabled() {
        sharedActions.waitForVisible(temperatureInput);
        return sharedActions.isElementEnabled(driver, temperatureInput);
    }

    public void enterThermometerId(String thermometerId) throws InterruptedException {
        sharedActions.sendKeysAndEnter(driver, thermometerIdInput, thermometerId);
    }

    public void waitForTemperatureFieldToBeEnabled() {
        sharedActions.waitForEnabled(temperatureInput);
    }

    public void waitForContinueButtonToBeEnabled() {
        sharedActions.waitForEnabled(continueButton);
    }

    public boolean isContinueButtonEnabled() {
        return sharedActions.isElementEnabled(driver, continueButton);
    }

    public void verifyFieldErrorMessage(String name, String message) {
        var dataTestId = "";
        if ("thermometer ID".equalsIgnoreCase(name)) {
            dataTestId = "device-id-validation-error";
        }
        var matError = matErrorDataTestId(dataTestId);
        sharedActions.waitForVisible(matError);
        Assertions.assertEquals(message, sharedActions.getText(matError));
    }

}
