package com.arcone.biopro.distribution.irradiation.verification.ui.pages;

import com.arcone.biopro.testing.frontend.core.PageElement;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Slf4j
@Lazy
@Component
public class CloseIrradiationPage extends IrradiationPage {

    // Close Irradiation specific locators
    private final By pageTitleLocator = By.xpath("//h3//span[contains(text(),'Close Irradiation')]");
    private final By selectAllUnitsButtonLocator = By.id("selectAllActionBtn");
    private final By removeUnitButtonLocator = By.id("removeActionBtn");
    private final By recordInspectionLocator = By.id("visualInspecActionBtn");
    private final By recordInspectionModalTitleLocator = By.xpath("//biopro-record-visual-inspection-modal/h1[contains(text(),'Record Inspection')]");
    private final By irradiatedStatusButtonLocator = By.id("irradiatedActionBtn");
    private final By notIrradiatedStatusButtonLocator = By.id("noIrradiatedActionBtn");
    private final By submitRecordInspectionLocator = By.id("submitActionBtn");
    private final By cancelRecordInspectionLocator = By.id("cancelActionBtn");

    @Override
    public boolean isLoaded() {
        try {
            PageElement pageTitle = driver.waitForElement(pageTitleLocator, 5);
            pageTitle.waitForVisible();
            return pageTitle.isDisplayed();
        } catch (org.openqa.selenium.TimeoutException e) {
            log.info("Close Irradiation page not loaded: {}", e.getMessage());
            return false;
        }
    }

    public void selectAllUnits() throws Exception {
        PageElement selectAllButton = driver.waitForElement(selectAllUnitsButtonLocator);
        selectAllButton.safeClick();
    }

    public void removeUnitFromBatch() throws Exception {
        PageElement removeUnitButton = driver.waitForElement(removeUnitButtonLocator);
        removeUnitButton.safeClick();
    }

    public void openRecordInspection() throws Exception {
        PageElement recordInspectionButton = driver.waitForElement(recordInspectionLocator);
        recordInspectionButton.safeClick();
    }

    public boolean recordInspectionWindowIsDisplayed(){
        PageElement recordInspectionModalTitle = driver.waitForElement(recordInspectionModalTitleLocator);
        recordInspectionModalTitle.waitForVisible();
        return recordInspectionModalTitle.isDisplayed();
    }

    public void selectIrradiatedStatus() throws Exception {
        PageElement irradiatedStatusButton = driver.waitForElement(irradiatedStatusButtonLocator);
        irradiatedStatusButton.safeClick();
    }

    public void selectNotIrradiatedStatus() throws Exception {
        PageElement notIrradiatedStatusButton = driver.waitForElement(notIrradiatedStatusButtonLocator);
        notIrradiatedStatusButton.safeClick();
    }

    public void submitRecordInspection() throws Exception {
        PageElement submitRecordInspectionButton = driver.waitForElement(submitRecordInspectionLocator);
        submitRecordInspectionButton.safeClick();
    }

    public void cancelRecordInspection() throws Exception {
        PageElement cancelRecordInspectionButton = driver.waitForElement(cancelRecordInspectionLocator);
        cancelRecordInspectionButton.safeClick();
    }
}
