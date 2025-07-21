package com.arcone.biopro.distribution.irradiation.verification.ui.pages.modals;

import com.arcone.biopro.testing.frontend.core.CommonPageFactory;
import com.arcone.biopro.testing.frontend.core.PageElement;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Slf4j
@Lazy
@Component
public class ConfirmationModalPage extends CommonPageFactory {

    private final By confirmationModalLocator = By.xpath("//mat-dialog-container[contains(@class, 'mat-mdc-dialog-container')]");
    private final By confirmationTitleLocator = By.xpath("//mat-dialog-container//fuse-confirmation-dialog//div[contains(@class, 'text-xl')]");
    private final By confirmationMessageLocator = By.xpath("//mat-dialog-container//fuse-confirmation-dialog//div[contains(@class, 'text-secondary')]");
    private final By confirmButtonLocator = By.id("confirmation-dialog-confirm-btn");

    @Override
    public boolean isLoaded() {
        return true;
    }


    public PageElement getConfirmationTitle() {
        try {
            PageElement title = driver.waitForElement(confirmationTitleLocator, 5);
            title.waitForVisible();
            return title;
        } catch (TimeoutException e) {
            log.warn("Confirmation modal title did not appear within the timeout.");
            throw e;
        } catch (Exception e) {
            log.error("Error while retrieving the confirmation modal title.", e);
            throw e;
        }
    }

    public PageElement getConfirmationMessage() {
        try {
            PageElement message = driver.waitForElement(confirmationMessageLocator);
            message.waitForVisible();
            return message;
        } catch (TimeoutException e) {
            log.warn("Confirmation modal message did not appear within the timeout.");
            throw e;
        } catch (Exception e) {
            log.error("Error while retrieving the confirmation modal message.", e);
            throw e;
        }
    }

    public String getConfirmationTitleText() {
        try {
            return getConfirmationTitle().getText().trim();
        } catch (Exception e) {
            log.error("Unable to retrieve the confirmation modal title text.", e);
            throw e;
        }
    }

    public String getConfirmationMessageText() {
        try {
            return getConfirmationMessage().getText().trim();
        } catch (Exception e) {
            log.error("Unable to retrieve the confirmation modal message text.", e);
            throw e;
        }
    }

    public void clickConfirmButton() {
        try {
            PageElement confirmButton = driver.waitForElement(confirmButtonLocator, 5);
            confirmButton.waitForClickable();
            confirmButton.click();
        } catch (TimeoutException e) {
            log.warn("Confirm button did not become clickable within the timeout.");
            throw e;
        } catch (Exception e) {
            log.error("Unable to click the Confirm button on the confirmation modal.", e);
            throw e;
        }
    }

    public void waitForConfirmationModal() {
        try {
            var confirmationModal = driver.waitForElement(confirmationModalLocator, 5);
            confirmationModal.waitForVisible();
        } catch (Exception e) {
            log.info("Error while waiting for confirmation modal");
            throw e;
        }
    }
}
