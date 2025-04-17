package com.arcone.biopro.distribution.recoveredplasmashipping.verification.pages;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AddCartonPage extends CommonPageFactory {

    @Autowired
    private SharedActions sharedActions;
    @Autowired
    private HomePage homePage;

    private final By addCartonHeader = By.xpath("//h3//span[contains(text(),'Add Carton Products')]");
    private final By cartonNumber = By.id("informationDetails-Carton-Number-value");
    private final By tareWeight = By.id("informationDetails-Tare-Weight-(g)-value");
    private final By totalVolume = By.id("informationDetails-Total-Volume-(L)-value");
    private final By minimumProducts = By.id("informationDetails-Minimum-Products-value");
    private final By maximumProducts = By.id("informationDetails-Maximum-Products-value");
    private final By clickBackBtn = By.id("backToShipmentBtn");
    private final By addUnitNumberInput = By.id("scanUnitNumberId");
    private final By addProductCodeInput = By.id("scanProductCodeId");

    private By addedProductCard(String unitNumber, String productCode) {
        return By.xpath(String.format("//biopro-unit-number-card//div[contains(text(),'%s')]/following-sibling::div/span[contains(text(),'%s')]", unitNumber, productCode));
    }

    @Override
    public boolean isLoaded() {
        return sharedActions.isElementVisible(addCartonHeader);
    }


    public void waitForLoad() {
        sharedActions.waitForVisible(addCartonHeader);
    }

    public void verifyCartonDetails(Map<String, String> table) {
        Assert.assertTrue(sharedActions.getText(cartonNumber).contains(table.get("Carton Number Prefix")));
        Assert.assertEquals(table.get("Tare Weight"), sharedActions.getText(tareWeight));
        Assert.assertEquals(table.get("Total Volume"), sharedActions.getText(totalVolume));

//        To be enabled when DIS-339 be integrated
//        Assert.assertEquals(table.get("Minimum Products"), sharedActions.getText(minimumProducts));
//        Assert.assertEquals(table.get("Maximum Products"), sharedActions.getText(maximumProducts));
    }

    public void clickBack() {
        sharedActions.click(clickBackBtn);
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

    public boolean verifyProductIsPacked(String unitNumber, String productCode) {
        try {
            sharedActions.waitForVisible(addedProductCard(unitNumber, productCode));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
