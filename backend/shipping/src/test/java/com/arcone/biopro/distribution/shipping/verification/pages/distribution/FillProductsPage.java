package com.arcone.biopro.distribution.shipping.verification.pages.distribution;

import com.arcone.biopro.distribution.shipping.verification.pages.CommonPageFactory;
import com.arcone.biopro.distribution.shipping.verification.pages.SharedActions;
import lombok.extern.slf4j.Slf4j;
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

    @FindBy(id = "productCodeId")
    private WebElement productCodeInput;

    @FindBy(id = "inspection-satisfactory")
    private WebElement visualInspectionSatisfactory;

    @FindBy(id = "inspection-unsatisfactory")
    private WebElement visualInspectionUnsatisfactory;

    @FindBy(id = "backBtn")
    private WebElement backButton;

    @Override
    public boolean isLoaded() {
        return sharedActions.isElementVisible(fillProductsHeader);
    }

    private String formatUnitLocator(String unit) {
        return String.format("//p-table[@id='prodTableId']//td[normalize-space()='%s']", unit);
    }

    private String formatProductCodeLocator(String productCode) {
        return String.format("//p-table[@id='prodTableId']//td[normalize-space()='%s']", productCode);
    }

    public void addUnitWithProductCode(String unit, String productCode) throws InterruptedException {
        log.info("Adding unit {} with product code {}.", unit, productCode);
        sharedActions.sendKeys(unitNumberInput, unit);
        sharedActions.sendKeys(productCodeInput, productCode);
        sharedActions.waitLoadingAnimation();
    }

    public void defineVisualInspection(String visualInspection) {
        log.info("Defining visual inspection as {}.", visualInspection);
        WebElement element = "satisfactory".equalsIgnoreCase(visualInspection) ? visualInspectionSatisfactory : visualInspectionUnsatisfactory;
        sharedActions.click(element);
    }

    public void ensureProductIsAdded(String unit, String productCode) {
        log.info("Ensuring product with unit {} and product code {} is added.", unit, productCode);

        String unitLocator = this.formatUnitLocator(unit);
        String productCodeLocator = this.formatProductCodeLocator(productCode);

        sharedActions.locateXpathAndWaitForVisible(unitLocator, this.driver);
        sharedActions.locateXpathAndWaitForVisible(productCodeLocator, this.driver);
    }

    public void ensureProductIsNotAdded(String unit, String productCode) throws InterruptedException {
        log.info("Ensuring product with unit {} and product code {} wasn't added.", unit, productCode);

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

}
