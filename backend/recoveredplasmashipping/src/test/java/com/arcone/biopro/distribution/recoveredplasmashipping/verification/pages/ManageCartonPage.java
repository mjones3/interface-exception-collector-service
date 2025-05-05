package com.arcone.biopro.distribution.recoveredplasmashipping.verification.pages;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ManageCartonPage extends CommonPageFactory {

    @Autowired
    private SharedActions sharedActions;
    @Autowired
    private HomePage homePage;

    private final By manageCartonHeader = By.xpath("//h3//span[contains(text(),'Manage Carton Products')]");
    private final By cartonNumber = By.id("informationDetails-Carton-Number-value");
    private final By tareWeight = By.id("informationDetails-Carton-Tare-Weight-(g)-value");
    private final By totalVolume = By.id("informationDetails-Total-Volume-(L)-value");
    private final By minimumProducts = By.id("informationDetails-Minimum-Products-value");
    private final By maximumProducts = By.id("informationDetails-Maximum-Products-value");
    private final By addUnitNumberInput = By.id("scanUnitNumberId");
    private final By addProductCodeInput = By.id("scanProductCodeId");
    private final By addVerifyUnitNumberInput = By.xpath("//biopro-verify-recovered-plasma-products//*[@id='scanUnitNumberId']");
    private final By addVerifyProductCodeInput = By.xpath("//biopro-verify-recovered-plasma-products//*[@id='scanProductCodeId']");
    private final By submitButton = By.id("submitActionBtn");
    private final By nextButton = By.id("manageCartonNextBtn");
    private final By verifyTab = By.xpath("//biopro-verify-recovered-plasma-products");
    private final By closeCartonButton = By.id("closeCartonBtn");
    private final By backShipmentDetailsBtn = By.id("backActionBtn");
    private final By backSearchBtn = By.id("backToSearchBtn");

    private By addedProductCard(String unitNumber, String productCode) {
        return By.xpath(String.format("//biopro-unit-number-card//div[contains(text(),'%s')]/following-sibling::div/span[contains(text(),'%s')]", unitNumber, productCode));
    }

    @Override
    public boolean isLoaded() {
        return sharedActions.isElementVisible(manageCartonHeader);
    }


    public void waitForLoad() {
        sharedActions.waitForVisible(manageCartonHeader);
    }

    public void verifyCartonDetails(Map<String, String> table) {
        Assert.assertTrue(sharedActions.getText(cartonNumber).contains(table.get("Carton Number Prefix")));
        Assert.assertEquals(table.get("Tare Weight"), sharedActions.getText(tareWeight));
        Assert.assertEquals(table.get("Total Volume"), sharedActions.getText(totalVolume));
        Assert.assertEquals(table.get("Minimum Products"), sharedActions.getText(minimumProducts));
        Assert.assertEquals(table.get("Maximum Products"), sharedActions.getText(maximumProducts));
    }

    public void clickBackToSearch() {
        sharedActions.click(backSearchBtn);
    }

    public void navigateToCarton(String cartonId) throws InterruptedException {
        var url = String.format("/recovered-plasma/%s/carton-details", cartonId);

        homePage.goTo();
        sharedActions.navigateTo(url);
    }

    public void addProduct(String unitNumber, String productCode) {
        sharedActions.sendKeys(addUnitNumberInput, unitNumber);
        sharedActions.sendKeys(addProductCodeInput, productCode);
    }
    public void verifyProduct(String unitNumber, String productCode) {
        sharedActions.sendKeys(addVerifyUnitNumberInput, unitNumber);
        sharedActions.sendKeys(addVerifyProductCodeInput, productCode);
    }

    public boolean verifyProductIsPacked(String unitNumber, String productCode) {
        try {
            sharedActions.waitForVisible(addedProductCard(unitNumber, productCode));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void clickSubmit() {
        sharedActions.click(submitButton);
    }

    public boolean verifyNextButtonIs(String enabledDisabled) {
        if (enabledDisabled.equalsIgnoreCase("enabled")){
            return sharedActions.isElementEnabled(driver, nextButton);
        } else if (enabledDisabled.equalsIgnoreCase("disabled")){
            return !sharedActions.isElementEnabled(driver, nextButton);
        } else {
            Assert.fail("Invalid option enabled/disabled");
            return false;
        }
    }

    public void clickNext() {
        sharedActions.click(nextButton);
    }

    public void waitForVerifyTab() {
        sharedActions.waitForVisible(verifyTab);
        sharedActions.waitForEnabled(verifyTab);
    }

    public boolean verifyCloseButtonIs(String enabledDisabled) {
        if (enabledDisabled.equalsIgnoreCase("enabled")){
            return sharedActions.isElementEnabled(driver, closeCartonButton);
        } else if (enabledDisabled.equalsIgnoreCase("disabled")){
            return !sharedActions.isElementEnabled(driver, closeCartonButton);
        } else {
            Assert.fail("Invalid option enabled/disabled");
            return false;
        }
    }

    public void clickBackToShipmentDetails() {
        sharedActions.click(backShipmentDetailsBtn);
    }

    public void closeCarton() {
        sharedActions.click(closeCartonButton);
    }
}
