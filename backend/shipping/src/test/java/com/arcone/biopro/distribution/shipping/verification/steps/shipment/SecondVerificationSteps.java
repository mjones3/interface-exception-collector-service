package com.arcone.biopro.distribution.shipping.verification.steps.shipment;

import com.arcone.biopro.distribution.shipping.verification.pages.SharedActions;
import com.arcone.biopro.distribution.shipping.verification.pages.distribution.HomePage;
import com.arcone.biopro.distribution.shipping.verification.pages.distribution.ShipmentDetailPage;
import com.arcone.biopro.distribution.shipping.verification.pages.distribution.VerifyProductsPage;
import com.arcone.biopro.distribution.shipping.verification.support.ApiHelper;
import com.arcone.biopro.distribution.shipping.verification.support.SharedContext;
import com.arcone.biopro.distribution.shipping.verification.support.TestUtils;
import com.arcone.biopro.distribution.shipping.verification.support.controllers.ShipmentTestingController;
import com.arcone.biopro.distribution.shipping.verification.support.graphql.GraphQLMutationMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@SpringBootTest
public class SecondVerificationSteps {

    @Autowired
    private SharedContext context;

    @Autowired
    private ApiHelper apiHelper;

    @Autowired
    ShipmentTestingController shipmentTestingController;

    @Autowired
    VerifyProductsPage verifyProductsPage;


    @Value("${save.all.screenshots}")
    private boolean saveAllScreenshots;

    @Value("${ui.shipment-details.url}")
    private String shipmentDetailsUrl;

    @Value("${default.employee.id}")
    private String defaultEmployeeId;

    @Autowired
    private HomePage homePage;
    @Autowired
    private SharedActions sharedActions;
    @Autowired
    private ShipmentDetailPage shipmentDetailPage;

    private Map verifyItemResponse;
    private LinkedHashMap verifiedProductsList;

    @Then("I should be redirected to the verify products page.")
    public void shouldBeRedirectedToVerifyProductsPage() {
        verifyProductsPage.isPageOpen(context.getShipmentId().toString());
    }

    @Then("I should be redirected to verify products page with {string} tab active.")
    public void shouldBeRedirectedToVerifyProductsPageWithTabActive(String tab) {
        verifyProductsPage.isPageTabOpen(context.getShipmentId().toString(), tab);
    }

    @Then("I can see the Order Information Details and the Shipping Information Details.")
    public void checkPageContent() {
        verifyProductsPage.viewPageContent();
    }

    @Then("I can see the Order Information Details and the Shipping Information Details as requested.")
    public void checkPageContentDetails(DataTable dataTable) {
        verifyProductsPage.viewPageContent(dataTable);
    }

    @When("I scan the unit {string} with product code {string}.")
    public void scanUnitAndProduct(String unitNumber, String productCode) throws InterruptedException {
        verifyProductsPage.scanUnitAndProduct(unitNumber, productCode);
    }

    @When("I rescan the unit {string} with product code {string}.")
    public void rescanUnitAndProduct(String unitNumber, String productCode) throws InterruptedException {
        this.scanUnitAndProduct(unitNumber, productCode);
        context.setTotalPacked(context.getTotalPacked() - 1);
    }

    @Then("I should see the unit added to the verified products table.")
    public void checkVerifiedProductIsPresent() throws InterruptedException {
        Assert.assertTrue(verifyProductsPage.isProductVerified(context.getUnitNumber(), context.getProductCode()));
        context.setTotalVerified(context.getTotalVerified() + 1);
    }

    @And("I should see the log of verified products being updated.")
    public void verifyLogInProgress() {
        String progress = verifyProductsPage.getProductsProgressLog();
        var progressText = String.format("%s/%s", context.getTotalVerified(), context.getTotalPacked());
        Assert.assertEquals(progress, progressText);
    }

    @And("The complete shipment option should be enabled.")
    public void theCompleteShipmentOptionShouldBeEnabled() {
        Assert.assertTrue(verifyProductsPage.isCompleteShipmentButtonEnabled());
    }

    @And("I should not see the unit added to the verified products table.")
    public void verifyProductsNotAdded() {
        Assert.assertTrue(verifyProductsPage.isProductNotVerified(context.getUnitNumber(), context.getProductCode()));
    }

    @And("The complete shipment option should not be enabled.")
    public void theCompleteShipmentOptionShouldNotBeEnabled() {
        Assert.assertTrue(verifyProductsPage.isCompleteShipmentButtonDisabled());
    }

    @And("I am on the verify products page.")
    public void iAmOnTheVerifyProductsPage() throws InterruptedException {
        homePage.goTo();
        verifyProductsPage.goToPage(context.getShipmentId().toString());
    }

    @When("I focus out leaving {string} empty.")
    public void iFocusOn(String field) {
        verifyProductsPage.focusOnField(field);

    }

    @Then("I should see a field validation error message {string}.")
    public void iShouldSeeFieldValidationErrorMessage(String error) {
        verifyProductsPage.checkFieldErrorMessage(error);
    }

    @When("I {string} the {string} {string}.")
    public void scanOrTypeUnitOrProduct(String action, String field, String value) throws InterruptedException {
        verifyProductsPage.scanOrTypeField(action, field, value);
    }

    @And("The {string} field should be {string}.")
    public void verifyFieldEnabledDisabled(String field, String status) {
        if (status.equalsIgnoreCase("enabled")) {
            Assert.assertTrue(verifyProductsPage.isFieldEnabled(field));
        } else if (status.equalsIgnoreCase("disabled")) {
            Assert.assertFalse(verifyProductsPage.isFieldEnabled(field));
        } else {
            Assert.fail("Invalid status provided");
        }
    }

    @Then("I should see a notification dialog with the message {string}.")
    public void verifyDialogMessage(String message) {
        Assert.assertTrue(verifyProductsPage.isDialogMessage(message));
    }

    @And("I should have an option to acknowledge the notification.")
    public void iShouldHaveAnOptionToAcknowledgeTheNotification() {
        Assert.assertTrue(verifyProductsPage.isDialogAcknowledgementButtonEnabled());
    }

    @And("I should see a list of products grouped by the following statuses:")
    public void iShouldSeeAListOfProductsGroupedByTheFollowingStatuses(List<Map<String, String>> statuses) {
        Assert.assertTrue(verifyProductsPage.isProductListGroupedByStatus(statuses));
    }

    @When("I verify each one of the tabs.")
    public void iVerifyEachOneOfTheTabs() {
        // Empty step. This is a placeholder to make the scenario more readable.
    }

    @Then("I should see the following products.")
    public void iShouldSeeTheFollowingProducts(List<Map<String, String>> products) throws InterruptedException {
        Assert.assertTrue(verifyProductsPage.verifyNotifiedProducts(products));
    }

    @And("The verified unit {string} is unsuitable with status {string} and message {string}.")
    public void theVerifiedUnitIsUnsuitableWithStatus(String unitNumber, String status, String message) {
        shipmentTestingController.updateShipmentItemStatus(context.getShipmentId(), unitNumber, status, message);
        context.setToBeRemoved(context.getToBeRemoved() + 1);
    }

    @And("I am on the verify products page with {string} tab active.")
    public void iAmOnTheVerifyProductsPageWithTabActive(String tab) throws InterruptedException {
        homePage.goTo();
        verifyProductsPage.goToPageAndTab(context.getShipmentId().toString(), tab);
    }

    @And("I should see a notification banner: {string}.")
    public void iShouldSeeANotificationBanner(String message) {
        Assert.assertTrue(verifyProductsPage.isNotificationBannerVisible(message));
    }

    @Then("I should see the unit {string} with code {string} added to the removed products section with unsuitable status {string}.")
    public void checkUnitRemoved(String unitNumber, String productCode, String status) {
        Assert.assertTrue(verifyProductsPage.isProductRemoved(unitNumber, productCode, status));
        context.setTotalRemoved(context.getTotalRemoved() + 1);
    }

    @Then("I should not see the unit {string} with code {string} added to the removed products section with unsuitable status {string}.")
    public void checkUnitNotRemoved(String unitNumber, String productCode, String status) {
        Assert.assertTrue(verifyProductsPage.isProductNotRemoved(unitNumber, productCode, status));
        context.setTotalRemoved(context.getTotalRemoved() + 1);
    }

    @And("I should see the log of removed products being updated.")
    public void checkRemovedCount() {
        String progress = verifyProductsPage.getProductsProgressLog();
        var progressText = String.format("%s/%s", context.getToBeRemoved(), context.getTotalRemoved());
        Assert.assertEquals(progress.replace(" ", ""), progressText.replace(" ", ""));
    }

    @And("The fill more products option should be {string}.")
    public void theFillMoreProductsOptionShouldBeEnabled(String status) {
        if (status.equalsIgnoreCase("enabled")) {
            Assert.assertTrue(verifyProductsPage.isFillMoreProductsButtonEnabled());
        } else if (status.equalsIgnoreCase("disabled")) {
            Assert.assertTrue(verifyProductsPage.isFillMoreProductsButtonDisabled());
        } else {
            Assert.fail("Invalid status provided");
        }
    }


    @And("I should see the verified products section empty.")
    public void iShouldSeeTheVerifiedProductsSectionEmpty() {
        String progress = verifyProductsPage.getProductsProgressLog();
        var progressText = String.format("%s/%s", 0, context.getTotalPacked());
        Assert.assertEquals(progress.replace(" ", ""), progressText);
    }

    @When("I confirm the notification dialog")
    public void iConfirmTheNotificationDialog() {
        verifyProductsPage.confirmNotificationDialog();
    }

    @When("I request to cancel the second verification process.")
    public void iRequestToCancelTheSecondVerificationProcess() {
        context.setCancelSecondVerificationResponse(apiHelper.graphQlRequest(GraphQLMutationMapper.cancelSecondVerification(context.getShipmentId(), "test-emplyee-id"), "cancelSecondVerification"));
        log.debug("Cancel second verification completed: {}", context.getCancelSecondVerificationResponse());
        Assert.assertNotNull(context.getCancelSecondVerificationResponse());
    }

    @Then("I should receive status {string} with type {string} and message {string}.")
    public void iShouldReceiveStatusWithTheMessage(String status, String notificationType, String message) {
        var responseStatus = context.getCancelSecondVerificationResponse().get("ruleCode");
        Assert.assertEquals(status, responseStatus);

        var notifications = (List<Map>) context.getCancelSecondVerificationResponse().get("notifications");

        var responseNotificationType = notifications.getFirst().get("notificationType");
        Assert.assertEquals(notificationType, responseNotificationType);

        var responseMessage = notifications.getFirst().get("message");
        Assert.assertEquals(message, responseMessage);
    }

    @And("I should receive a redirect address to {string}.")
    public void iShouldReceiveARedirectAddressTo(String page) {
        var url = switch (page) {
            case "Shipment Details Page" ->
                shipmentDetailsUrl.replace("{shipmentId}", context.getShipmentId().toString());
            default -> throw new IllegalArgumentException("Page not mapped");
        };

        var links = (Map) context.getCancelSecondVerificationResponse().get("_links");
        Assert.assertEquals(url, links.get("next"));
    }

    @When("I request to confirm the cancellation.")
    public void iRequestToConfirmTheCancellation() {
        context.setCancelSecondVerificationResponse(apiHelper.graphQlRequest(GraphQLMutationMapper.confirmCancelSecondVerification(context.getShipmentId(), "test-emplyee-id"), "confirmCancelSecondVerification"));
        log.debug("Confirm cancel second verification completed: {}", context.getCancelSecondVerificationResponse());
        Assert.assertNotNull(context.getCancelSecondVerificationResponse());
    }

    @When("I choose to cancel the second verification process.")
    public void iChooseToCancelTheSecondVerificationProcess() throws InterruptedException {
        log.debug("Cancelling second verification process.");
        verifyProductsPage.cancelSecondVerification();
    }

    @When("I choose to cancel the confirmation.")
    public void iChooseToCancelTheConfirmation() {
        verifyProductsPage.cancelSecondVerificationCancellation();
    }

    @Then("The confirmation dialog should be closed.")
    public void theConfirmationDialogShouldBeClosed() {
        sharedActions.confirmationDialogIsNotVisible();
    }

    @And("The verified units should remain in the verified products table.")
    public void theVerifiedUnitsShouldRemainInTheVerifiedProductsTable() throws InterruptedException {
        Assert.assertTrue(verifyProductsPage.isProductVerified(context.getUnitNumber(), context.getProductCode()));
    }

    @When("I confirm the cancellation.")
    public void iConfirmTheCancellation() {
        verifyProductsPage.confirmCancelSecondVerification();
    }

    @And("I should not have any verified product in the shipment.")
    public void iShouldNotHaveAnyVerifiedProductInTheShipment() {
        Assert.assertTrue(verifyProductsPage.isProductNotVerified(context.getUnitNumber(), context.getProductCode()));
    }

    @And("The verify option should be enabled.")
    public void theVerifyOptionShouldBeEnabled() {
        shipmentDetailPage.checkVerifyProductsButtonIsVisible();
    }

    @And("The product code should not be available.")
    public void theProductCodeShouldNotBeAvailable() {
        verifyProductsPage.verifyProductCodeInputVisible(false);
    }

    @Then("I {string} see the list of verified products added including {string} and {string}.")
    public void iShouldSeeTheListOfVerifiedProductsAddedIncludingAnd(String shouldOrNot, String unitNumber, String productCodeOrDescription) throws InterruptedException {
        if (shouldOrNot.equalsIgnoreCase("should")) {
            Assert.assertTrue(verifyProductsPage.isProductVerified(unitNumber, productCodeOrDescription));
        } else if (shouldOrNot.equalsIgnoreCase("should not")) {
            Assert.assertFalse(verifyProductsPage.isProductVerified(unitNumber, productCodeOrDescription));
        } else {
            Assert.fail("Invalid option for should / should not");
        }
    }

    @When("I scan the unit {string}.")
    public void iScanTheUnit(String un) throws InterruptedException {
        verifyProductsPage.scanUnit(un);
    }

    @When("I verify a product with the unit number {string}, product code {string}.")
    public void iVerifyAProductWithTheUnitNumberProductCode(String un, String productCode) {
        shipmentTestingController.verifyItem(context.getShipmentId(), un, productCode, defaultEmployeeId);
    }

    @Then("The product unit number {string} and product code {string} should be verified in the shipment.")
    public void theProductUnitNumberAndProductCodeShouldBeVerifiedInTheShipment(String un, String productCodes) {
        var productCodeList = TestUtils.getCommaSeparatedList(productCodes);
        var match = false;
        for (String productCode : productCodeList) {
            match = context.getVerifiedProductsList().stream().anyMatch(
                verifiedProduct -> verifiedProduct.get("productCode").equals(productCode)
            && verifiedProduct.get("unitNumber").equals(un));
        }
        Assert.assertTrue(match);
    }
}
