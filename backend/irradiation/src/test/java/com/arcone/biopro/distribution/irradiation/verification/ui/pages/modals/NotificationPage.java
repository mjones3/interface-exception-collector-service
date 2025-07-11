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
public class NotificationPage extends CommonPageFactory {
    private final By notificationDialogLocator = By.id("notifications-dialog");
    private final By acceptButtonLocator = By.id("btnAccept");
    private final By noPreservativeProductsListSectionLocator = By.id("no-preservative-products-section");
    private final By noInLineProductsListSectionLocator = By.id("no-in-line-filter-products-section");
    private final By noPreservativeProductsListSectionTitleLocator = By.xpath("//div[@id='no-preservative-products-section']/p");
    private final By noInLineProductsListSectionTitleLocator = By.xpath("//div[@id='no-in-line-filter-products-section']/p");

    private By unitNumberCardInSection(String section, String unitNumber) {
        String sectionId = switch (section) {
            case "No inline filter products" -> "no-in-line-filter-products-section";
            case "No Preservative products" -> "no-preservative-products-section";
            default -> "";
        };
        return By.xpath("//div[@id='" + sectionId + "']//fuse-card[@id='" + unitNumber + "']");
    }

    @Override
    public boolean isLoaded() {
        return true;
    }

    public boolean notificationDialogIsDisplayed() {
        try {
            PageElement title = driver.waitForElement(notificationDialogLocator, 5);
            title.waitForVisible();
            return title.isDisplayed();
        } catch (TimeoutException e) {
            log.warn("Confirmation modal title did not appear within the timeout.");
            throw e;
        } catch (Exception e) {
            log.error("Error while retrieving the confirmation modal title.", e);
            throw e;
        }
    }

    public void clickAcceptButton() {
        try {
            PageElement confirmButton = driver.waitForElement(acceptButtonLocator, 5);
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

    public boolean sectionIsDisplayed(String section) {
        PageElement sectionElement;
        switch (section) {
            case "No inline filter products":
                sectionElement = driver.waitForElement(noInLineProductsListSectionLocator);
                break;
            case "No Preservative products":
                sectionElement = driver.waitForElement(noPreservativeProductsListSectionLocator);
                break;
            default:
                return false;
        }
        return sectionElement.isDisplayed();
    }

    public String getSectionTitle(String section) {
        PageElement title;
        switch (section) {
            case "No inline filter products":
                title = driver.waitForElement(noInLineProductsListSectionTitleLocator);
                break;
            case "No Preservative products":
                title = driver.waitForElement(noPreservativeProductsListSectionTitleLocator);
                break;
            default:
                return "";
        }
        return title.getText();
    }

    public boolean unitNumberIsDisplayedInSection(String section, String unitNumber) {
        PageElement unitNumberCard = driver.waitForElement(unitNumberCardInSection(section, unitNumber));
        return unitNumberCard.isDisplayed();
    }
}
