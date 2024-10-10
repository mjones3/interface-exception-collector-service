package com.arcone.biopro.distribution.shipping.verification.pages.distribution;

import com.arcone.biopro.distribution.shipping.verification.pages.CommonPageFactory;
import com.arcone.biopro.distribution.shipping.verification.pages.SharedActions;
import com.arcone.biopro.distribution.shipping.verification.support.DatabaseService;
import com.arcone.biopro.distribution.shipping.verification.support.TestUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FillProductsPage extends CommonPageFactory {

    @Autowired
    private SharedActions sharedActions;

    @FindBy(xpath = "//h3[normalize-space()='Fill Products']")
    private WebElement fillProductsHeader;


    @FindBy(id = "inCheckDigit")
    private WebElement checkDigitInput;

    @FindBy(xpath = "//*[@id=\"inCheckDigit\"]/../../../..//mat-error")
    private WebElement checkDigitError;


    @FindBy(id = "inspection-satisfactory-input")
    private WebElement visualInspectionSatisfactory;

    @FindBy(id = "inspection-unsatisfactory-input")
    private WebElement visualInspectionUnsatisfactory;

    // Static locators

    private static final String productCodeInput = "productCodeId";
    private static final String unitNumberInput = "inUnitNumber";
    private static final String visualInspectionSatisfactoryOption = "//*[@id='inspection-satisfactory']";
    private static final String visualInspectionUnsatisfactoryOption = "//*[@id='inspection-unsatisfactory']";

    @FindBy(id = "backBtn")
    private WebElement backButton;

    @Autowired
    private DatabaseService databaseService;

    @Override
    public boolean isLoaded() {
        return sharedActions.isElementVisible(fillProductsHeader);
    }

    private String formatUnitLocator(String unit) {
        unit = TestUtils.removeUnitNumberScanDigits(unit);
        return String.format("//p-table[@id='prodTableId']//td[normalize-space()='%s']", unit);
    }

    private String formatProductCodeLocator(String productCode) {
        productCode = TestUtils.removeProductCodeScanDigits(productCode);
        return String.format("//p-table[@id='prodTableId']//td[normalize-space()='%s']", productCode);
    }

    public void addUnitWithProductCode(String unit, String productCode) throws InterruptedException {
        log.info("Adding unit {} with product code {}.", unit, productCode);
        sharedActions.sendKeys(this.driver, By.id(unitNumberInput), unit);
        sharedActions.sendKeysAndEnter(this.driver, By.id(productCodeInput), productCode);
        sharedActions.waitLoadingAnimation();
    }

    public void addUnitWithDigitAndProductCode(String unit, String checkDigit, String productCode, boolean checkDigitEnabled) throws InterruptedException {
        log.info("Adding unit {} with digit {} and product code {}.", unit, checkDigit, productCode);

        sharedActions.sendKeys(this.driver, By.id(unitNumberInput), unit);
        if (checkDigitEnabled && !unit.startsWith("=")) {
            sharedActions.sendKeys(checkDigitInput, checkDigit);
        }
        sharedActions.sendKeysAndEnter(this.driver, By.id(productCodeInput), productCode);
        sharedActions.waitLoadingAnimation();
    }

    public void defineVisualInspection(String visualInspection) throws InterruptedException {
        log.info("Defining visual inspection as {}.", visualInspection);
        if ("satisfactory".equalsIgnoreCase(visualInspection)) {
            sharedActions.click(this.driver, By.xpath(visualInspectionSatisfactoryOption));
        } else {
            sharedActions.click(this.driver, By.xpath(visualInspectionUnsatisfactoryOption));
        }
    }

    public void assertVisualInspectionIs(String enabledDisabled) {
        log.info("Asserting visual inspection is {}.", enabledDisabled);
        enabledDisabled = enabledDisabled.toLowerCase();
        Assert.assertTrue("enabled".equalsIgnoreCase(enabledDisabled) || "disabled".equalsIgnoreCase(enabledDisabled));
        if ("enabled".equalsIgnoreCase(enabledDisabled)) {
            Assert.assertTrue(visualInspectionSatisfactory.isEnabled());
            Assert.assertTrue(visualInspectionUnsatisfactory.isEnabled());
        } else {
            Assert.assertFalse(visualInspectionSatisfactory.isEnabled());
            Assert.assertFalse(visualInspectionUnsatisfactory.isEnabled());
        }
    }

    public void ensureProductIsAdded(String unit, String productCode) {
        log.info("Ensuring product with unit {} and product code {} is added.", unit, productCode);

        unit = TestUtils.removeUnitNumberScanDigits(unit);
        productCode = TestUtils.removeProductCodeScanDigits(productCode);

        String unitLocator = this.formatUnitLocator(unit);
        String productCodeLocator = this.formatProductCodeLocator(productCode);

        sharedActions.waitForVisible(By.xpath(unitLocator));
        sharedActions.waitForVisible(By.xpath(productCodeLocator));
    }

    public void ensureProductIsNotAdded(String unit, String productCode) throws InterruptedException {
        log.info("Ensuring product with unit {} and product code {} wasn't added.", unit, productCode);

        unit = TestUtils.removeUnitNumberScanDigits(unit);
        productCode = TestUtils.removeProductCodeScanDigits(productCode);

        String unitLocator = this.formatUnitLocator(unit);
        String productCodeLocator = this.formatProductCodeLocator(productCode);
        sharedActions.waitLoadingAnimation();
        sharedActions.waitForNotVisible(By.xpath(unitLocator));
        sharedActions.waitForNotVisible(By.xpath(productCodeLocator));
    }

    public void clickBackButton() {
        log.info("Clicking back button.");
        sharedActions.click(backButton);
    }

    public void assertCheckDigitErrorIs(String expectedError) {
        log.info("Asserting check digit error is {}.", expectedError);
        if (expectedError.isEmpty()) {
            sharedActions.waitForNotVisible(checkDigitError);
        } else {
            sharedActions.waitForVisible(checkDigitError);
            Assert.assertEquals(expectedError.toLowerCase(), checkDigitError.getText().toLowerCase());
        }
    }

}
