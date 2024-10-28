package com.arcone.biopro.distribution.shipping.verification.pages.distribution;

import com.arcone.biopro.distribution.shipping.verification.pages.CommonPageFactory;
import com.arcone.biopro.distribution.shipping.verification.pages.SharedActions;
import com.arcone.biopro.distribution.shipping.verification.support.TestUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class FillProductsPage extends CommonPageFactory {

    @Autowired
    private SharedActions sharedActions;

    @FindBy(xpath = "//h3[normalize-space()='Fill Products']")
    private WebElement fillProductsHeader;

    private String checkDigitError = "//*[@id='inCheckDigit']/../../../..//mat-error";


    @FindBy(id = "inspection-satisfactory")
    private WebElement visualInspectionSatisfactory;

    @FindBy(id = "inspection-unsatisfactory")
    private WebElement visualInspectionUnsatisfactory;

    // Static locators

    private static final String checkDigitInput = "inCheckDigit";
    private static final String productCodeInput = "productCodeId";
    private static final String unitNumberInput = "inUnitNumber";
    private static final String visualInspectionSatisfactoryOption = "//*[@id='inspection-satisfactory']";
    private static final String visualInspectionUnsatisfactoryOption = "//*[@id='inspection-unsatisfactory']";
    private static final String backButton = "backBtn";
    private static final String discardDialogCancelButton = "//*[@id='mat-mdc-dialog-0']//*[@id='recordUnsatisfactoryVisualInspectionCancelActionBtn']";
    private static final String discardDialogSubmitButton = "recordUnsatisfactoryVisualInspectionSubmitActionBtn";
    private static final String dialogLocator = "//*[@id='mat-mdc-dialog-0']";
    private static final String dialogHeaderLocator = "//*[@id='mat-mdc-dialog-0']//h1";
    private static final String dialogSubtitleLocator = "//*[@id='mat-mdc-dialog-0']//h2";
    private static final String reasonsLocator = "//*[@id='mat-mdc-dialog-0']//*[@id='recordUnsatisfactoryVisualInspectionReasons']//biopro-action-button";
    private static final String discardComments = "recordUnsatisfactoryVisualInspectionCommentsTextArea";

    // Dynamic locators

    private String discardReasonButton(String reason) {
        return String.format("%sactionBtn", reason.replace(" ", "_").toUpperCase());
    }

    @Override
    public boolean isLoaded() {
        return sharedActions.isElementVisible(fillProductsHeader);
    }

    private String formatUnitLocator(String unit) {
        unit = TestUtils.removeUnitNumberScanDigits(unit);
        return String.format("//biopro-unit-number-card[@ng-reflect-unit-number='%s']", unit);
    }

    private String formatProductCodeLocator(String productCode) {
        productCode = TestUtils.removeProductCodeScanDigits(productCode);
        return String.format("//biopro-unit-number-card[@ng-reflect-product-name='%s']", productCode);
    }

    private String formatProductInspectionLocator(String inspection) {
        return String.format("//biopro-unit-number-card[@ng-reflect-visual-inspection='%s']", inspection.toUpperCase());
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
            sharedActions.sendKeysAndTab(this.driver, By.id(checkDigitInput), checkDigit);
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

    public void clickBackButton() throws InterruptedException {
        log.info("Clicking back button.");
        sharedActions.click(this.driver, By.id(backButton));
    }

    public void assertCheckDigitErrorIs(String expectedError) throws InterruptedException {
        log.info("Asserting check digit error is {}.", expectedError);
        if (expectedError.isEmpty()) {
            sharedActions.waitForNotVisible(By.xpath(checkDigitError));
        } else {
            sharedActions.waitForVisible(By.xpath(checkDigitError));
            String msg = sharedActions.getText(By.xpath(checkDigitError));
            Assert.assertEquals(expectedError.toLowerCase(), msg.toLowerCase());
        }
    }

    public void addUnitWithDigit(String unitNumber, String checkDigit) throws InterruptedException {
        log.info("Adding unit {} with digit {}.", unitNumber, checkDigit);
        sharedActions.sendKeys(this.driver, By.id(unitNumberInput), unitNumber);
        sharedActions.sendKeysAndTab(this.driver, By.id(checkDigitInput), checkDigit);
    }

    public void verifyVisualInspectionDialog(String header , String title){
        log.info("Verifying visual Inspection Dialog: {} , {}", header , title);

        String headerText = sharedActions.getText(By.xpath(dialogHeaderLocator));
        String subtitleText = sharedActions.getText(By.xpath(dialogSubtitleLocator));
        Assert.assertEquals(header.toUpperCase(),headerText.toUpperCase());
        Assert.assertEquals(title.toUpperCase(), subtitleText.toUpperCase());
    }

    public void verifyDiscardReasons(String reasons){
        log.debug("Verifying discardReasons: {}" , reasons);
        sharedActions.waitForVisible(By.xpath(reasonsLocator));
        List<WebElement> reasonList = wait.until(e -> e.findElements(By.xpath(reasonsLocator)));
        var reasonStr = reasonList.stream().map(WebElement::getText).toList();
        Assert.assertEquals(String.join(",", reasonStr),reasons);

    }

    public void clickDiscardDialogCancelButton() throws InterruptedException {
        log.debug("Clicking discard cancel button.");
        sharedActions.click(this.driver, By.xpath(discardDialogCancelButton));
    }

    public void verifyDiscardDialogIsClosed(){
        log.debug("Verifying visual Inspection Dialog close");
        sharedActions.waitForNotVisible(By.xpath(dialogLocator));
    }

    public void selectDiscardReason(String reason) throws InterruptedException {
        log.debug("Selecting discard reason: {}" , reason);
        sharedActions.click(this.driver, By.id(discardReasonButton(reason)));
    }

    public void verifyDiscardCommentIsRequired() {
        log.debug("Verifying discard comment is required");
        sharedActions.waitForVisible(By.id(discardComments));
        Assert.assertTrue(sharedActions.isRequired(By.id(discardComments)));
    }

    public void verifyDiscardSubmitIs(String option) {
        log.debug("Verifying discard submit is: {}" , option);
        sharedActions.waitForVisible(By.id(discardDialogSubmitButton));
        if(option.equalsIgnoreCase("enabled")){
            sharedActions.waitForEnabled(By.id(discardDialogSubmitButton));
        }else{
            sharedActions.waitForDisabled(By.id(discardDialogSubmitButton));
        }
    }

    public void fillDiscardComments(String comments) throws InterruptedException {
        log.debug("Filling discard comments: {}" , comments);
        sharedActions.sendKeys(this.driver, By.id(discardComments), comments);
    }

    public void clickDiscardDialogSubmitButton() throws InterruptedException {
        log.debug("Clicking discard submit button.");
        sharedActions.click(this.driver, By.id(discardDialogSubmitButton));
    }

    public void assertProductInspectionIs(String inspection) {
        log.debug("Asserting product inspection is {}.", inspection);
        sharedActions.waitForVisible(By.xpath(formatProductInspectionLocator(inspection)));
    }
}
