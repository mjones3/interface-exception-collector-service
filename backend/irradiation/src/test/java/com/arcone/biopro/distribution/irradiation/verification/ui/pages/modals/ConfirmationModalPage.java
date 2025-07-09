package com.arcone.biopro.distribution.irradiation.verification.ui.pages.modals;

import com.arcone.biopro.testing.frontend.core.CommonPageFactory;
import com.arcone.biopro.testing.frontend.core.PageElement;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Slf4j
@Lazy
@Component
public class ConfirmationModalPage extends CommonPageFactory {

    // Define locators
    private final By confirmationModalLocator = By.xpath("//mat-dialog-container[contains(@class, 'mat-mdc-dialog-container')]");
    private final By confirmationMessageLocator = By.cssSelector(".mat-mdc-dialog-surface .text-secondary");
    private final By confirmButtonLocator = By.xpath("//button[.//span[contains(text(),'Confirm')]]");
    private final By contentHeaderMessageLocator = By.xpath("//biopro-reason-modal//mat-dialog-content//div[@class='p-8'][1]");


    @Override
    public boolean isLoaded() {
        return true;
    }


    public PageElement getConfirmationMessage() {
        PageElement message = driver.waitForElement(confirmationMessageLocator, 5);
        message.waitForVisible();
        return message;
    }

    public PageElement getContentHeaderMessage() {
        PageElement message = driver.waitForElement(contentHeaderMessageLocator, 5);
        message.waitForVisible();
        return message;
    }


    public void closeConfirmationMessage() {
        try {
            PageElement confirmButton = driver.waitForElement(confirmButtonLocator);
            confirmButton.waitForVisible();
            confirmButton.click();
        } catch (Exception e) {
            log.info("Unable to close the modal");
            throw e;
        }
    }

    public void waitForConfirmationModal() {
        try {
            var confirmationModal = driver.waitForElement(confirmationModalLocator, 5);
            confirmationModal.waitForVisible();
            confirmationModal.waitForClickable();
        } catch (Exception e) {
            log.info("Error while waiting for confirmation modal");
            throw e;
        }
    }
}
