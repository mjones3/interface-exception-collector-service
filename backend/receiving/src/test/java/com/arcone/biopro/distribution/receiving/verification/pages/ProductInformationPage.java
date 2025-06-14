package com.arcone.biopro.distribution.receiving.verification.pages;

import com.arcone.biopro.distribution.receiving.verification.support.SharedContext;
import com.arcone.biopro.distribution.receiving.verification.support.TestUtils;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProductInformationPage extends CommonPageFactory {

    @Autowired
    HomePage homePage;

    @Autowired
    private SharedActions sharedActions;

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private SharedContext sharedContext;

    private final By productInformationPageTitle = By.xpath("//h3/span[contains(text(),'Product Information')]");
    private final By unitNumberInput = By.id("unitNumberInput");
    private final By aboRhInput = By.id("aboRhInput");
    private final By productCodeInput = By.id("productCodeInput");
    private final By expirationDateInput = By.id("expirationDateInput");
    private final By addProductBtn = By.xpath("//button[@id='btnAdd']");
    private final By resetBtn = By.xpath("//button[@id='btnReset']");
    private final By completeImportBtn = By.xpath("//button[@id='productInformationSubmitActionButton']");
    private final By cancelBtn = By.xpath("//button[@id='productInformationCancelActionButton']");

    private By licenseStatusSelect(String licenseStatus) {
        return By.xpath(String.format("//button[starts-with(@id,'%s')]", licenseStatus));
    }

    private By visualInspectionStatusSelect(String visualInspectionStatus) {
        return By.xpath(String.format("//button[starts-with(@id,'%s')]", visualInspectionStatus));
    }

    private By productAdded(String unitNumber, String productCode) {
        return By.xpath(String.format("//td/span[contains(text(), '%s')]/../../td/span[contains(text(), '%s')]", unitNumber, productCode));
    }


    @Override
    public boolean isLoaded() {
        return sharedActions.isElementVisible(productInformationPageTitle);
    }


    public void goTo(String importId) throws InterruptedException {
        homePage.goTo();

        String url = "/receiving/{importId}/product-information";
        sharedActions.navigateTo(url.replace("{importId}", importId));
    }

    public void goTo() throws InterruptedException {
        goTo(sharedContext.getCreateImportResponse().get("id").toString());
    }

    public void waitForPageToLoad() {
        sharedActions.waitForVisible(productInformationPageTitle);
    }

    public void scanUnitNumber(String unitNumber) throws InterruptedException {
        sharedActions.sendKeysAndEnter(driver, unitNumberInput, unitNumber);
    }

    public void scanProductCode(String productCode) throws InterruptedException {
        sharedActions.sendKeysAndEnter(driver, productCodeInput, productCode);
    }

    public void scanBloodType(String bloodType) throws InterruptedException {
        sharedActions.sendKeysAndEnter(driver, aboRhInput, bloodType);
    }

    public void setExpirationDate(String expirationDate) throws InterruptedException {
        sharedActions.sendKeysAndEnter(driver, expirationDateInput, expirationDate);
    }

    public void selectLicenseStatus(String licenseStatus) {
        sharedActions.click(licenseStatusSelect(licenseStatus));
    }

    public void selectVisualInspection(String inspectionStatus) {
        sharedActions.click(visualInspectionStatusSelect(inspectionStatus));
    }

    public void addProduct() {
        sharedActions.click(addProductBtn);
    }

    public void verifyProductAdded(String unitNumber, String productCode, boolean expectVisible) {
        if (expectVisible) {
            sharedActions.waitForVisible(productAdded(testUtils.removeUnitNumberScanDigits(unitNumber), testUtils.removeProductCodeScanDigits(productCode)));
        } else {

            sharedActions.waitForNotVisible(productAdded(testUtils.removeUnitNumberScanDigits(unitNumber), testUtils.removeProductCodeScanDigits(productCode)));
        }
    }

    public boolean isAddProductButtonEnabled() {
        return sharedActions.isElementEnabled(driver, addProductBtn);
    }

    public boolean isCompleteImportButtonEnabled(boolean expectEnabled) {
        if(expectEnabled){
            sharedActions.waitForEnabled(completeImportBtn);
        }
        return sharedActions.isElementEnabled(driver, completeImportBtn);

    }

    public void completeImport() {
        sharedActions.waitForEnabled(completeImportBtn);
        sharedActions.click(completeImportBtn);
    }

    public void cancelImport() {
        sharedActions.click(cancelBtn);
    }
}
