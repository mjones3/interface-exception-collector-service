package com.arcone.biopro.distribution.irradiation.verification.ui.pages.modals;

import com.arcone.biopro.testing.frontend.core.CommonPageFactory;
import com.arcone.biopro.testing.frontend.core.PageElement;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Lazy
@Component
public class AcknowledgeModalPage extends CommonPageFactory {

    // Define locators
    private final By acknowledgeModalLocator = By.id("productDetailAcknowledgeModal");
    private final By acknowledgeTitleLocator = By.id("acknowledgeTitle");
    private final By acknowledgeDescriptionLocator = By.id("acknowledgeDescription");
    private final By acknowledgeSubtitleLocator = By.id("acknowledgeSubtitle");
    private final By acknowledgeDetailsLocator = By.id("acknowledgeDetails");
    private final By confirmButtonLocator = By.xpath("//button[.//span[contains(text(),'Confirm')]]");


    @Override
    public boolean isLoaded() {
        return true;
    }

    public PageElement getAcknowledgeTitle() {
        PageElement title = driver.waitForElement(acknowledgeTitleLocator, 5);
        title.waitForVisible();
        return title;
    }

    public PageElement getAcknowledgeDescription() {
        PageElement description = driver.waitForElement(acknowledgeDescriptionLocator, 5);
        description.waitForVisible();
        return description;
    }

    public PageElement getAcknowledgeSubtitle() {
        PageElement subtitle = driver.waitForElement(acknowledgeSubtitleLocator, 5);
        subtitle.waitForVisible();
        return subtitle;
    }

    public List<String> getAcknowledgeDetails() {
        PageElement detailsContainer = driver.waitForElement(acknowledgeDetailsLocator, 5);
        detailsContainer.waitForVisible();
        List<PageElement> detailElements = detailsContainer.findChildElements(By.className("ng-star-inserted"));
        List<String> details = new ArrayList<>();
        for (PageElement detailElement : detailElements) {
            details.add(detailElement.getText());
        }
        return details;
    }

    public void closeAcknowledgeMessage() {
        try {
            PageElement confirmButton = driver.waitForElement(confirmButtonLocator);
            confirmButton.waitForVisible();
            confirmButton.click();
        } catch (Exception e) {
            log.info("Unable to close the modal");
            throw e;
        }
    }

    public void waitForAcknowledgeModal() {
        try {
            var confirmationModal = driver.waitForElement(acknowledgeModalLocator, 5);
            confirmationModal.waitForVisible();
            confirmationModal.waitForClickable();
        } catch (Exception e) {
            log.info("Error while waiting for confirmation modal");
            throw e;
        }
    }
}
