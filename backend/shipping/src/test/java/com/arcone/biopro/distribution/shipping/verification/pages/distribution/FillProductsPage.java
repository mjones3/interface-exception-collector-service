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

    @FindBy(id = "inUnitNumber")
    private WebElement unitNumberInput;

    @FindBy(id = "inCheckDigit")
    private WebElement checkDigitInput;

    @FindBy(xpath = "//*[@id=\"inCheckDigit\"]/../../../..//mat-error")
    private WebElement checkDigitError;

    @FindBy(id = "productCodeId")
    private WebElement productCodeInput;

    @FindBy(id = "inspection-satisfactory-input")
    private WebElement visualInspectionSatisfactory;

    @FindBy(id = "inspection-unsatisfactory-input")
    private WebElement visualInspectionUnsatisfactory;

    @FindBy(id = "inspection-satisfactory")
    private WebElement visualInspectionSatisfactoryOption;

    @FindBy(id = "inspection-unsatisfactory")
    private WebElement visualInspectionUnsatisfactoryOption;

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
        sharedActions.sendKeys(unitNumberInput, unit);
        sharedActions.sendKeys(productCodeInput, productCode);
        sharedActions.waitLoadingAnimation();
    }

    public void addUnitWithDigitAndProductCode(String unit, String checkDigit, String productCode, boolean checkDigitEnabled) throws InterruptedException {
        log.info("Adding unit {} with digit {} and product code {}.", unit, checkDigit, productCode);

        sharedActions.sendKeys(unitNumberInput, unit);
        if (checkDigitEnabled && !unit.startsWith("=")) {
            sharedActions.sendKeys(checkDigitInput, checkDigit);
        }
        sharedActions.sendKeys(productCodeInput, productCode);
        sharedActions.waitLoadingAnimation();
    }

    public void defineVisualInspection(String visualInspection) {
        log.info("Defining visual inspection as {}.", visualInspection);
        WebElement element = "satisfactory".equalsIgnoreCase(visualInspection) ? visualInspectionSatisfactoryOption : visualInspectionUnsatisfactoryOption;
        sharedActions.click(element);
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

        sharedActions.locateXpathAndWaitForVisible(unitLocator, this.driver);
        sharedActions.locateXpathAndWaitForVisible(productCodeLocator, this.driver);
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
