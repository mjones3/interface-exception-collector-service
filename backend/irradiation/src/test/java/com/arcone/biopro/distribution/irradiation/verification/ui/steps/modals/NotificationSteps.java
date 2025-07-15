package com.arcone.biopro.distribution.irradiation.verification.ui.steps.modals;

import com.arcone.biopro.distribution.irradiation.verification.ui.pages.modals.NotificationPage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import org.junit.Assert;

import static org.junit.Assert.assertTrue;

@RequiredArgsConstructor
public class NotificationSteps {

    private final NotificationPage notificationPage;

    @Then("I see a Notifications pop-up")
    public void iSeeANotificationsPopUp() {
        assertTrue(notificationPage.notificationDialogIsDisplayed());
    }

    @And("I accept the notifications")
    public void iAcceptTheNotifications() {
        notificationPage.clickAcceptButton();
    }

    @And("In the notifications pop-up I see a section for {string} with the title {string}")
    public void inTheNotificationsPopUpISeeASectionFor(String section, String title) {
        assertTrue(notificationPage.sectionIsDisplayed(section));
        Assert.assertEquals(title, notificationPage.getSectionTitle(section));
    }

    @And("In the notifications pop-up, In the section for {string} I see a card for the unit number {string}")
    public void inTheNotificationsPopUpInTheSectionForISeeACardForTheUnitNumber(String section, String unitNumber) {
        assertTrue(notificationPage.unitNumberIsDisplayedInSection(section, unitNumber));
    }
}
