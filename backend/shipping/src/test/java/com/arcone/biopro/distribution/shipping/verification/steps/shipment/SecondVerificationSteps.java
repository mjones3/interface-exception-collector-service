package com.arcone.biopro.distribution.shipping.verification.steps.shipment;

import com.arcone.biopro.distribution.shipping.verification.pages.SharedActions;
import com.arcone.biopro.distribution.shipping.verification.pages.distribution.HomePage;
import com.arcone.biopro.distribution.shipping.verification.pages.distribution.ShipmentDetailPage;
import com.arcone.biopro.distribution.shipping.verification.pages.distribution.VerifyProductsPage;
import com.arcone.biopro.distribution.shipping.verification.support.ApiHelper;
import com.arcone.biopro.distribution.shipping.verification.support.GraphQLMutationMapper;
import com.arcone.biopro.distribution.shipping.verification.support.ScreenshotService;
import com.arcone.biopro.distribution.shipping.verification.support.controllers.ShipmentTestingController;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@SpringBootTest
public class SecondVerificationSteps {

    private Long shipmentId;
    private String unitNumber;
    private String productCode;
    private Integer totalPacked = 0;
    private Integer totalVerified = 0;
    private Integer totalRemoved = 0;
    private Integer toBeRemoved = 0;
    private Map cancelSecondVerificationResponse;

    @Autowired
    private ApiHelper apiHelper;

    @Autowired
    ShipmentTestingController shipmentTestingController;

    @Autowired
    VerifyProductsPage verifyProductsPage;

    @Autowired
    private ScreenshotService screenshot;

    @Value("${save.all.screenshots}")
    private boolean saveAllScreenshots;

    @Value("${ui.shipment-details.url}")
    private String shipmentDetailsUrl;

    @Autowired
    private HomePage homePage;
    @Autowired
    private SharedActions sharedActions;
    @Autowired
    private ShipmentDetailPage shipmentDetailPage;

    @Given("I have a shipment for order {string} with the unit {string} and product code {string} {string}.")
    public void createPackedShipment(String orderNumber, String unitNumber, String productCode, String itemStatus){
        this.unitNumber = unitNumber;
        this.productCode = productCode;
        this.shipmentId = shipmentTestingController.createPackedShipment(orderNumber, List.of(unitNumber),List.of(productCode), itemStatus);

        Assert.assertNotNull(this.shipmentId);
        this.totalPacked = 1;

    }

    @Given("I have a shipment for order {string} with the units {string} and product codes {string} {string}.")
    public void createPackedShipmentMultipleUnits(String orderNumber, String unitNumbers, String productCodes, String itemStatus){
        var units = Arrays.stream(unitNumbers.split(",")).toList();
        var productCodeList = Arrays.stream(productCodes.split(",")).toList();

        this.unitNumber = units.getFirst();
        this.productCode = productCodeList.getFirst();
        this.shipmentId = shipmentTestingController.createPackedShipment(orderNumber,units,productCodeList, itemStatus);

        Assert.assertNotNull(this.shipmentId);
        this.totalPacked = units.size();

        if (itemStatus.equalsIgnoreCase("verified")){
            this.totalVerified = units.size();
        }

    }

    @Then("I should be redirected to the verify products page.")
    public void shouldBeRedirectedToVerifyProductsPage() {
        verifyProductsPage.isPageOpen(this.shipmentId.toString());
    }

    @Then("I should be redirected to verify products page with {string} tab active.")
    public void shouldBeRedirectedToVerifyProductsPageWithTabActive(String tab) {
        verifyProductsPage.isPageTabOpen(this.shipmentId.toString(), tab);
    }

    @Then("I can see the Order Information Details and the Shipping Information Details.")
    public void checkPageContent(){
        verifyProductsPage.viewPageContent();
        screenshot.attachConditionalScreenshot(saveAllScreenshots);
    }

    @When("I scan the unit {string} with product code {string}.")
    public void scanUnitAndProduct(String unitNumber, String productCode) throws InterruptedException {
        verifyProductsPage.scanUnitAndProduct(unitNumber, productCode);
    }

    @When("I rescan the unit {string} with product code {string}.")
    public void rescanUnitAndProduct(String unitNumber, String productCode) throws InterruptedException {
        this.scanUnitAndProduct(unitNumber, productCode);
        totalPacked--;
    }

    @Then("I should see the unit added to the verified products table.")
    public void checkVerifiedProductIsPresent() {
        Assert.assertTrue(verifyProductsPage.isProductVerified(unitNumber, productCode));
        this.totalVerified++;
    }

    @And("I should see the log of verified products being updated.")
    public void verifyLogInProgress() {
        String progress = verifyProductsPage.getProductsProgressLog();
        var progressText = String.format("%s/%s",totalVerified,totalPacked);
        Assert.assertEquals(progress, progressText);
    }

    @And("The complete shipment option should be enabled.")
    public void theCompleteShipmentOptionShouldBeEnabled() {
        Assert.assertTrue(verifyProductsPage.isCompleteShipmentButtonEnabled());
    }

    @And("I should not see the unit added to the verified products table.")
    public void verifyProductsNotAdded() {
        Assert.assertTrue(verifyProductsPage.isProductNotVerified(unitNumber, productCode));
    }

    @And("The complete shipment option should not be enabled.")
    public void theCompleteShipmentOptionShouldNotBeEnabled() {
        Assert.assertTrue(verifyProductsPage.isCompleteShipmentButtonDisabled());
    }

    @And("I am on the verify products page.")
    public void iAmOnTheVerifyProductsPage() throws InterruptedException {
        homePage.goTo();
        verifyProductsPage.goToPage(this.shipmentId.toString());
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
        shipmentTestingController.updateShipmentItemStatus(this.shipmentId, unitNumber, status, message);
        this.toBeRemoved++;
    }

    @And("I am on the verify products page with {string} tab active.")
    public void iAmOnTheVerifyProductsPageWithTabActive(String tab) throws InterruptedException {
        homePage.goTo();
        verifyProductsPage.goToPageAndTab(this.shipmentId.toString(), tab);
    }

    @And("I should see a notification banner: {string}.")
    public void iShouldSeeANotificationBanner(String message) {
        Assert.assertTrue(verifyProductsPage.isNotificationBannerVisible(message));
    }

    @Then("I should see the unit {string} with code {string} added to the removed products section with unsuitable status {string}.")
    public void checkUnitRemoved(String unitNumber, String productCode, String status) {
        Assert.assertTrue(verifyProductsPage.isProductRemoved(unitNumber, productCode, status));
        this.totalRemoved++;
    }

    @Then("I should not see the unit {string} with code {string} added to the removed products section with unsuitable status {string}.")
    public void checkUnitNotRemoved(String unitNumber, String productCode, String status) {
        Assert.assertTrue(verifyProductsPage.isProductNotRemoved(unitNumber, productCode, status));
        this.totalRemoved++;
    }

    @And("I should see the log of removed products being updated.")
    public void checkRemovedCount() {
        String progress = verifyProductsPage.getProductsProgressLog();
        var progressText = String.format("%s/%s",toBeRemoved,totalRemoved);
        Assert.assertEquals(progress.replace(" ",""), progressText.replace(" ",""));
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
        var progressText = String.format("%s/%s",0,totalPacked);
        Assert.assertEquals(progress.replace(" ",""), progressText);
    }

    @When("I confirm the notification dialog")
    public void iConfirmTheNotificationDialog() {
        verifyProductsPage.confirmNotificationDialog();
    }

    @When("I request to cancel the second verification process.")
    public void iRequestToCancelTheSecondVerificationProcess() {
        this.cancelSecondVerificationResponse = apiHelper.graphQlRequest(GraphQLMutationMapper.cancelSecondVerification(this.shipmentId, "test-emplyee-id"), "cancelSecondVerification");
        log.debug("Cancel second verification completed: {}", this.cancelSecondVerificationResponse);
        Assert.assertNotNull(this.cancelSecondVerificationResponse);
    }

    @Then("I should receive status {string} with type {string} and message {string}.")
    public void iShouldReceiveStatusWithTheMessage(String status, String notificationType, String message) {
        var responseStatus = this.cancelSecondVerificationResponse.get("ruleCode");
        Assert.assertEquals(status, responseStatus);

        var notifications = (List<Map>) this.cancelSecondVerificationResponse.get("notifications");

        var responseNotificationType = notifications.getFirst().get("notificationType");
        Assert.assertEquals(notificationType, responseNotificationType);

        var responseMessage = notifications.getFirst().get("message");
        Assert.assertEquals(message, responseMessage);
    }

    @And("I should receive a redirect address to {string}.")
    public void iShouldReceiveARedirectAddressTo(String page) {
        var url = switch (page) {
            case "Shipment Details Page" -> shipmentDetailsUrl.replace("{shipmentId}", this.shipmentId.toString());
            default -> throw new IllegalArgumentException("Page not mapped");
        };

        var links = (Map) this.cancelSecondVerificationResponse.get("_links");
        Assert.assertEquals(url, links.get("next"));
    }

    @When("I request to confirm the cancellation.")
    public void iRequestToConfirmTheCancellation() {
        this.cancelSecondVerificationResponse = apiHelper.graphQlRequest(GraphQLMutationMapper.confirmCancelSecondVerification(this.shipmentId, "test-emplyee-id"), "confirmCancelSecondVerification");
        log.debug("Confirm cancel second verification completed: {}", this.cancelSecondVerificationResponse);
        Assert.assertNotNull(this.cancelSecondVerificationResponse);
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
    public void theVerifiedUnitsShouldRemainInTheVerifiedProductsTable() {
        Assert.assertTrue(verifyProductsPage.isProductVerified(unitNumber, productCode));
    }

    @When("I confirm the cancellation.")
    public void iConfirmTheCancellation() {
        verifyProductsPage.confirmCancelSecondVerification();
    }

    @And("I should not have any verified product in the shipment.")
    public void iShouldNotHaveAnyVerifiedProductInTheShipment() {
        Assert.assertTrue(verifyProductsPage.isProductNotVerified(unitNumber, productCode));
    }

    @And("The verify option should be enabled.")
    public void theVerifyOptionShouldBeEnabled() {
        shipmentDetailPage.checkVerifyProductsButtonIsVisible();
    }
}
