package com.arcone.biopro.distribution.inventory.verification.steps;

import com.arcone.biopro.distribution.inventory.adapter.in.socket.dto.*;
import com.arcone.biopro.distribution.inventory.common.TestUtil;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhCriteria;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.MessageType;
import com.arcone.biopro.distribution.inventory.domain.model.vo.Quarantine;
import com.arcone.biopro.distribution.inventory.infrastructure.persistence.InventoryEntity;
import com.arcone.biopro.distribution.inventory.verification.utils.InventoryUtil;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class RsocketSteps {

    @Autowired
    RSocketRequester.Builder builder;

    @Autowired
    InventoryUtil inventoryUtil;

    @Value("${spring.rsocket.server.port}")
    Integer port;

    Mono<GetAvailableInventoryResponseDTO> getAvailableInventoryResponseDTOMonoResult;

    GetAvailableInventoryResponseDTO getAvailableInventoryResponseDTOResult;

    Mono<InventoryValidationResponseDTO> inventoryValidationResponseDTOMonoResult;

    private static RSocketRequester requester;

    List<AvailableInventoryCriteriaDTO> inventoryCriteriaList;

    @Before
    public void setupOnce() {
        requester = builder
            .tcp("localhost", port);

        inventoryCriteriaList = new ArrayList<>();
    }

    @Given("I have {string} products of family {string} with ABORh {string} in location {string} and that will expire in {string} days")
    public void iHaveOfTheOfTheBloodTypeInThe(String quantity, String productFamily, String aboRh, String location, String days) {
        createMultipleProducts(quantity, productFamily, aboRh, location, days, InventoryStatus.AVAILABLE, "FROZEN");
    }


    @Given("I have one product with {string}, {string} and {string} in {string} status")
    public void iHaveOneProductWithAndInStatus(String unitNumber, String productCode, String location, String status) {
        int days = "EXPIRED".equals(status) ? -1 : 1;
        InventoryStatus inventoryStatus = "EXPIRED".equals(status) ? InventoryStatus.AVAILABLE : InventoryStatus.valueOf(status);

        var inventory = inventoryUtil.newInventoryEntity(unitNumber, productCode, inventoryStatus);
        inventory.setLocation(location);
        inventory.setIsLabeled(true);
        inventory.setExpirationDate(LocalDateTime.now().plusDays(days));
        inventoryUtil.saveInventory(inventory);
    }

    @Given("I have one product with {string}, {string} and {string} in {string} status with reason {string} and comments {string}")
    public void iHaveOneProductWithAndInStatus(String unitNumber, String productCode, String location, String status, String statusReason, String comments) {
        int days = "EXPIRED".equals(status) ? -1 : 1;
        InventoryStatus inventoryStatus = "EXPIRED".equals(status) ? InventoryStatus.AVAILABLE : InventoryStatus.valueOf(status);

        var inventory = inventoryUtil.newInventoryEntity(unitNumber, productCode, inventoryStatus);
        inventory.setLocation(location);
        inventory.setExpirationDate(LocalDateTime.now().plusDays(days));
        inventory.setStatusReason(statusReason);
        inventory.setComments(comments);
        inventory.setIsLabeled(true);
        inventoryUtil.saveInventory(inventory);
    }

    @When("I select {string} of the blood type {string}")
    public void iSelectOfTheOfTheBloodType(String productFamily, String aboRh) {
        inventoryCriteriaList.add(new AvailableInventoryCriteriaDTO(productFamily, AboRhCriteria.valueOf(aboRh), null));
    }

    @When("I request available inventories for family with the following parameters:")
    public void iRequestOfTheOfTheBloodType(DataTable dataTable) {
        List<Map<String, String>> parameters = dataTable.asMaps(String.class, String.class);
        var parameter = parameters.getFirst();
        var builder = AvailableInventoryCriteriaDTO.builder();
        builder.productFamily(parameter.get("Product Family"));
        builder.bloodType(AboRhCriteria.valueOf(parameter.get("Abo Rh Type")));
        if(parameter.containsKey("Temperature Category")) {
            builder.temperatureCategory(parameter.get("Temperature Category"));
        }

        getAvailableInventoryResponseDTOMonoResult = requester
            .route("getAvailableInventoryWithShortDatedProducts")
            .data(new GetAvailableInventoryCommandDTO(parameter.get("Location"), List.of(builder.build())))
            .retrieveMono(GetAvailableInventoryResponseDTO.class);
    }

    @When("I request available inventories in location {string}")
    public void iRequestToLocation(String location) {
        getAvailableInventoryResponseDTOMonoResult = requester
            .route("getAvailableInventoryWithShortDatedProducts")
            .data(new GetAvailableInventoryCommandDTO(location, inventoryCriteriaList))
            .retrieveMono(GetAvailableInventoryResponseDTO.class);

        getAvailableInventoryResponseDTOResult = getAvailableInventoryResponseDTOMonoResult.block();
    }


    @When("I request {string} with {string} in the {string}")
    public void iRequestWithInThe(String unitNumber, String productCode, String location) {
        inventoryValidationResponseDTOMonoResult = requester
            .route("validateInventory")
            .data(new InventoryValidationRequest(unitNumber, productCode, location))
            .retrieveMono(InventoryValidationResponseDTO.class);
    }

    @Then("I receive {string} of total products and {string} of short date")
    public void iReceive(String quantityTotal, String quantityShortDate) {
        StepVerifier
            .create(getAvailableInventoryResponseDTOMonoResult)
            .consumeNextWith(message -> {
                var inventories = message.inventories().getFirst();

                int expectedShort = Integer.parseInt(quantityShortDate);
                int expectedTotal = Integer.parseInt(quantityTotal);

                int actualShort = inventories.shortDateProducts().size();
                int actualTotal = inventories.quantityAvailable();

                assertThat(actualShort)
                    .withFailMessage("Expected shortDateProducts size: %d but was %d", expectedShort, actualShort)
                    .isEqualTo(expectedShort);

                assertThat(actualTotal)
                    .withFailMessage("Expected quantityAvailable: %d but was %d", expectedTotal, actualTotal)
                    .isEqualTo(expectedTotal);
            })
            .verifyComplete();
    }

    @Then("I receive {string} groups")
    public void iReceiveGroups(String groupQuantity) {
        assertThat(getAvailableInventoryResponseDTOResult.inventories().size()).isEqualTo(Integer.parseInt(groupQuantity));

    }

    @And("I receive a group of product family {string} and abo rh criteria {string} with {string} inventories and {string} product short date listed")
    public void iReceiveAGroupOfProductFamilyAndAboRhCriteriaWithIventoriesAndProductShortDateListed(String productFamily, String aboRhCriteria, String quantityAvailable, String quantityShortDate) {
        Inventory inventory = getAvailableInventoryResponseDTOResult.inventories().stream().filter(inv ->
                inv.productFamily().equals(productFamily) && inv.aboRh().equals(AboRhCriteria.valueOf(aboRhCriteria)))
            .findFirst()
            .orElse(null);

        assertThat(inventory).isNotNull();
        assertThat(inventory.quantityAvailable()).isEqualTo(Integer.parseInt(quantityAvailable));
        assertThat(inventory.shortDateProducts().size()).isEqualTo(Integer.parseInt(quantityShortDate));
    }

    @Then("I receive for {string} with {string} in the {string} a {string} message with {string} action and {string} reason and {string} message and details {string}")
    public void iReceiveForWithInTheAMessage(String unitNumber, String productCode, String location, String errorType, String action, String reason, String messageError, String details) {
        Integer errorCode = "".equals(errorType) ? null : MessageType.valueOf(errorType).getCode();
        StepVerifier
            .create(inventoryValidationResponseDTOMonoResult)
            .consumeNextWith(message -> {
                if (!MessageType.INVENTORY_NOT_EXIST.getCode().equals(errorCode)) {
                    assertThat(message.inventoryResponseDTO().unitNumber()).isEqualTo(unitNumber);
                    assertThat(message.inventoryResponseDTO().productCode()).isEqualTo(productCode);

                    if (!MessageType.INVENTORY_NOT_FOUND_IN_LOCATION.getCode().equals(errorCode)) {
                        assertThat(message.inventoryResponseDTO().locationCode()).isEqualTo(location);
                    }

                } else {
                    assertThat(message.inventoryResponseDTO()).isNull();
                }

                if (errorCode != null) {
                    assertThat(message.inventoryNotificationsDTO().getFirst().errorCode()).isEqualTo(errorCode);
                    assertThat(message.inventoryNotificationsDTO().getFirst().errorName()).isEqualTo(errorType);
                    assertThat(message.inventoryNotificationsDTO().getFirst().action()).isEqualTo(action);
                    assertThat(message.inventoryNotificationsDTO().getFirst().action()).isEqualTo(action);
                    assertThat(message.inventoryResponseDTO()).hasNoNullFieldsOrProperties();
                    assertThat(message.inventoryNotificationsDTO().getFirst().errorMessage()).isEqualTo(messageError);

                } else {
                    assertThat(message.inventoryNotificationsDTO().isEmpty()).isTrue();
                }
                if (MessageType.INVENTORY_IS_QUARANTINED.getCode().equals(errorCode)) {
                    var detailsList = Arrays.stream(details.split(",")).map(String::toString).map(String::trim).toList();
                    detailsList.forEach(
                        detail -> assertThat(message.inventoryNotificationsDTO().getFirst().details().contains(detail)).isTrue());
                    assertThat(message.inventoryNotificationsDTO().size()).isEqualTo(1);
                }

                if (MessageType.INVENTORY_IS_EXPIRED.getCode().equals(errorCode)) {
                    assertThat(message.inventoryNotificationsDTO().getFirst().reason()).isEqualTo(reason);
                }
                if (MessageType.INVENTORY_IS_UNSUITABLE.getCode().equals(errorCode)) {
                    assertThat(message.inventoryNotificationsDTO().getFirst().reason()).isEqualTo(reason);
                }
                log.debug("Received message from validate inventory {}", message);
            })
            .verifyComplete();
    }

    @And("I have one product with {string}, {string} and {string} in {string} status with quarantine reasons {string} and comments {string}")
    public void iHaveOneProductWithAndInStatusWithQuarantineReasonsAndComments(String unitNumber, String productCode, String location, String status, String quarantineReasons, String quarantineComments) {
        int days = InventoryStatus.DISCARDED.equals(InventoryStatus.valueOf(status)) ? -1 : 1;
        List<Quarantine> quarantines = Arrays.stream(quarantineReasons.split(",")).map(String::trim).map(reason -> new Quarantine(1L, reason, quarantineComments)).collect(Collectors.toList());

        var inventory = inventoryUtil.newInventoryEntity(unitNumber, productCode, InventoryStatus.valueOf(status));
        inventory.setExpirationDate(LocalDateTime.now().plusDays(days));
        inventory.setLocation(location);
        inventory.setQuarantines(quarantines);
        inventory.setStatusReason("ACTIVE_DEFERRAL");
        inventory.setComments(null);
        inventory.setIsLabeled(true);
        inventoryUtil.saveInventory(inventory);
    }

    @And("I have one product with {string}, {string} and {string} in {string} status with reason {string}")
    public void iHaveOneProductWithAndInStatusWithReason(String unitNumber, String productCode, String location, String status, String reason) {
        int days = "EXPIRED".equals(status) ? -1 : 1;
        InventoryStatus inventoryStatus = "EXPIRED".equals(status) ? InventoryStatus.AVAILABLE : InventoryStatus.valueOf(status);

        var inventory = inventoryUtil.newInventoryEntity(unitNumber, productCode, inventoryStatus);
        inventory.setLocation(location);
        inventory.setExpirationDate(LocalDateTime.now().plusDays(days));
        inventory.setStatusReason(reason);
        inventory.setIsLabeled(true);
        inventoryUtil.saveInventory(inventory);
    }

    @And("I have one product with {string}, {string} and {string} in {string} status and is unlabeled")
    public void iHaveOneProductWithAndInStatusAndIsUnlabeled(String unitNumber, String productCode, String location, String status) {
        int days = "EXPIRED".equals(status) ? -1 : 1;
        InventoryStatus inventoryStatus = "EXPIRED".equals(status) ? InventoryStatus.AVAILABLE : InventoryStatus.valueOf(status);

        var inventory = inventoryUtil.newInventoryEntity(unitNumber, productCode, inventoryStatus);
        inventory.setLocation(location);
        inventory.setExpirationDate(LocalDateTime.now().plusDays(days));
        inventory.setIsLabeled(false);
        inventoryUtil.saveInventory(inventory);
    }


    private void createInventory(String unitNumber, String productCode, String productFamily, AboRhType aboRhType, String location, Integer daysToExpire, InventoryStatus status, String statusReason, String comments) {
        var inventory = inventoryUtil.newInventoryEntity(unitNumber, productCode, status);
        inventory.setLocation(location);
        inventory.setExpirationDate(LocalDateTime.now().plusDays(daysToExpire));
        inventory.setProductFamily(productFamily);
        inventory.setAboRh(aboRhType);
        inventory.setStatusReason(statusReason);
        inventory.setComments(comments);
        inventory.setIsLabeled(true);
        inventoryUtil.saveInventory(inventory);
    }

    private void createMultipleProducts(String quantity, String productFamily, String aboRh, String location, String days, InventoryStatus status, String temperatureCategory) {
        int qty = Integer.parseInt(quantity);
        int daysToExpire = Integer.parseInt(days);
        AboRhType aboRhType = AboRhType.valueOf(aboRh);

        for (int i = 0; i < qty; i++) {
            InventoryEntity inventory = inventoryUtil.newInventoryEntity(
                TestUtil.randomString(13),
                "E0869V00",
                status
            );
            inventory.setLocation(location);
            inventory.setExpirationDate(LocalDateTime.now().plusDays(daysToExpire));
            inventory.setProductFamily(productFamily);
            inventory.setAboRh(aboRhType);
            inventory.setIsLabeled(true);
            inventory.setTemperatureCategory(temperatureCategory);
            inventoryUtil.saveInventory(inventory);
        }
    }

    @And("I have one product with {string}, {string} and {string} in {string} status with unsuitable reason {string}")
    public void iHaveOneProductWithAndInStatusWithUnsuitableReason(String unitNumber, String productCode, String location, String status, String reason) {
        var inventory = inventoryUtil.newInventoryEntity(unitNumber, productCode, InventoryStatus.valueOf(status));
        inventory.setExpirationDate(LocalDateTime.now().plusDays(1));
        inventory.setLocation(location);
        inventory.setUnsuitableReason(reason);
        inventory.setComments(null);
        inventory.setIsLabeled(true);
        inventoryUtil.saveInventory(inventory);
    }

    @And("I have {string} products of family {string} with ABORh {string} in location {string} and with temperature category {string} and that will expire in {string} days")
    public void iHaveProductsOfFamilyWithABORhInLocationAndWithProductCodeAndThatWillExpireInDays(String quantity, String productFamily, String aboRh, String location, String temperatureCategory, String days) {
        createMultipleProducts(quantity, productFamily, aboRh, location, days, InventoryStatus.AVAILABLE, temperatureCategory);
    }
}
