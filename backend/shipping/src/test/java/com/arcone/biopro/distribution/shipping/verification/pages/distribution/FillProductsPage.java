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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Component
@Slf4j
public class FillProductsPage extends CommonPageFactory {

    @Autowired
    private SharedActions sharedActions;

    @Autowired
    private HomePage homePage;

    @Value("${ui.shipment-fill-products.url}")
    private String fillProductsUrl;

    @FindBy(id = "inspection-satisfactory")
    private WebElement visualInspectionSatisfactory;

    @FindBy(id = "inspection-unsatisfactory")
    private WebElement visualInspectionUnsatisfactory;

    // Static locators

    private static final String fillProductsHeaderLocator = "//h3[normalize-space()='Manage Products']";
    private static final String checkDigitError = "//*[@id='inCheckDigit']/../../../..//mat-error";
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
    private static final String removeButtonLocator = "remove-btn";

    // Dynamic locators

    private String discardReasonButton(String reason) {
        return String.format("%sactionBtn", reason.replace(" ", "_").toUpperCase());
    }

    private String productButtonLocator(String unitNumber, String productCode) {
        return String.format("//biopro-unit-number-card//*[contains(text(),'%s')]/..//*[contains(text(),'%s')]", unitNumber, productCode);
    }

    private By availableProductButton(String product) {
        return By.xpath(String.format("//biopro-options-picker//button//*[contains(text(), '%s')]", product));
    }

    @Override
    public boolean isLoaded() {
        return sharedActions.isElementVisible(By.xpath(fillProductsHeaderLocator));
    }

    public void goTo(String shipmentId, String shipmentItemId) throws InterruptedException {
        homePage.goTo();

        var url = fillProductsUrl.replace("{shipmentId}", shipmentId).replace("{shipmentItemId}", shipmentItemId);

        log.debug("Navigating to the fill products page: {}", url);
        sharedActions.navigateTo(url);
        this.waitForLoad();
        assertTrue(isLoaded());
    }

    public void waitForLoad() {
        sharedActions.waitForVisible(By.xpath(fillProductsHeaderLocator));
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

    private String formatProductStatusLocator(String status) {
        return String.format("//biopro-unit-number-card[@ng-reflect-ineligible-status='%s']", status.toUpperCase());
    }

    public void addUnit(String unit) throws InterruptedException {
        log.info("Adding unit {}.", unit);
        sharedActions.sendKeys(this.driver, By.id(unitNumberInput), unit);
        sharedActions.waitLoadingAnimation();
    }
    public void addUnitWithProductCode(String unit, String productCode) throws InterruptedException {
        log.info("Adding unit {} with product code {}.", unit, productCode);
        sharedActions.sendKeys(this.driver, By.id(unitNumberInput), unit);
        sharedActions.sendKeys(this.driver, By.id(productCodeInput), productCode);
        sharedActions.waitLoadingAnimation();
    }

    public void addUnitWithDigitAndProductCode(String unit, String checkDigit, String productCode, boolean checkDigitEnabled) throws InterruptedException {
        log.info("Adding unit {} with digit {} and product code {}.", unit, checkDigit, productCode);

        sharedActions.sendKeys(this.driver, By.id(unitNumberInput), unit);
        if (checkDigitEnabled && !unit.startsWith("=")) {
            sharedActions.sendKeysAndTab(this.driver, By.id(checkDigitInput), checkDigit);
        }
        sharedActions.sendKeys(this.driver, By.id(productCodeInput), productCode);
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

        sharedActions.waitForVisible(By.xpath(productButtonLocator(unit, productCode)));
    }

    public void ensureProductIsNotAdded(String unit, String productCode) throws InterruptedException {
        log.info("Ensuring product with unit {} and product code {} wasn't added.", unit, productCode);

        unit = TestUtils.removeUnitNumberScanDigits(unit);
        productCode = TestUtils.removeProductCodeScanDigits(productCode);

        sharedActions.waitLoadingAnimation();
        sharedActions.waitForNotVisible(By.xpath(productButtonLocator(unit, productCode)));
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

    public void verifyVisualInspectionDialog(String header, String title) {
        log.info("Verifying visual Inspection Dialog: {} , {}", header, title);

        String headerText = sharedActions.getText(By.xpath(dialogHeaderLocator));
        String subtitleText = sharedActions.getText(By.xpath(dialogSubtitleLocator));
        Assert.assertEquals(header.toUpperCase(), headerText.toUpperCase());
        Assert.assertEquals(title.toUpperCase(), subtitleText.toUpperCase());
    }

    public void verifyDiscardReasons(String reasons) {
        log.debug("Verifying discardReasons: {}", reasons);
        sharedActions.waitForVisible(By.xpath(reasonsLocator));
        List<WebElement> reasonList = wait.until(e -> e.findElements(By.xpath(reasonsLocator)));
        var reasonStr = reasonList.stream().map(WebElement::getText).toList();
        Assert.assertEquals(String.join(",", reasonStr), reasons);

    }

    public void clickDiscardDialogCancelButton() throws InterruptedException {
        log.debug("Clicking discard cancel button.");
        sharedActions.click(this.driver, By.xpath(discardDialogCancelButton));
    }

    public void verifyDiscardDialogIsClosed() {
        log.debug("Verifying visual Inspection Dialog close");
        sharedActions.waitForNotVisible(By.xpath(dialogLocator));
    }

    public void selectDiscardReason(String reason) throws InterruptedException {
        log.debug("Selecting discard reason: {}", reason);
        sharedActions.click(this.driver, By.id(discardReasonButton(reason)));
    }

    public void verifyDiscardCommentIsRequired() {
        log.debug("Verifying discard comment is required");
        sharedActions.waitForVisible(By.id(discardComments));
        Assert.assertTrue(sharedActions.isRequired(By.id(discardComments)));
    }

    public void verifyDiscardSubmitIs(String option) {
        log.debug("Verifying discard submit is: {}", option);
        sharedActions.waitForVisible(By.id(discardDialogSubmitButton));
        if (option.equalsIgnoreCase("enabled")) {
            sharedActions.waitForEnabled(By.id(discardDialogSubmitButton));
        } else {
            sharedActions.waitForDisabled(By.id(discardDialogSubmitButton));
        }
    }

    public void fillDiscardComments(String comments) throws InterruptedException {
        log.debug("Filling discard comments: {}", comments);
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

    public boolean isCheckDigitFieldIsNotVisible() {
        log.debug("Checking if check digit field is not visible.");
        sharedActions.waitForNotVisible(By.id(checkDigitInput));
        return true;
    }

    public void cleanProductCodeField() {
        log.debug("Cleaning product code field.");
        sharedActions.clearField(By.id(productCodeInput));
    }

    public void cleanUnitNumberField() {
        log.debug("Cleaning unit number field.");
        sharedActions.clearField(By.id(unitNumberInput));
    }

    public void selectProduct(String unitNumber, String productCode) throws InterruptedException {
        log.debug("Selecting product {} with product code {}.", unitNumber, productCode);
        sharedActions.click(this.driver, By.xpath(productButtonLocator(TestUtils.removeUnitNumberScanDigits(unitNumber), TestUtils.removeProductCodeScanDigits(productCode))));
    }

    public void clickRemoveProductsButton() throws InterruptedException {
        log.debug("Clicking remove products button.");
        sharedActions.click(this.driver, By.id(removeButtonLocator));
    }

    public void assertProductStatusIs(String productStatus,boolean visible) {
        log.debug("Asserting product status is {}.", productStatus);
        if(visible){
            sharedActions.waitForVisible(By.xpath(formatProductStatusLocator(productStatus)));
        }else {
            sharedActions.waitForNotVisible(By.xpath(formatProductStatusLocator(productStatus)));
        }
    }

    public void checkAvailableProductButton(String product) {
        log.debug("Checking available product button for product {}.", product);
        sharedActions.waitForVisible(availableProductButton(product));
    }

    public void selectAvailableProduct(String product) {
        log.debug("Selecting available product {}.", product);
        sharedActions.click(availableProductButton(product));
    }
}
