package com.arcone.biopro.distribution.inventory.verification.steps;

import com.arcone.biopro.distribution.inventory.adapter.in.socket.dto.*;
import com.arcone.biopro.distribution.inventory.common.TestUtil;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhCriteria;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.MessageType;
import com.arcone.biopro.distribution.inventory.domain.model.vo.Quarantine;
import com.arcone.biopro.distribution.inventory.infrastructure.persistence.InventoryEntity;
import com.arcone.biopro.distribution.inventory.infrastructure.persistence.InventoryEntityRepository;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class RsocketSteps {

    @Autowired
    RSocketRequester.Builder builder;

    @Autowired
    InventoryEntityRepository inventoryEntityRepository;

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

    @Given("I have {string} of the {string} of the blood type {string} in the {string} will expire in {string} days")
    public void iHaveOfTheOfTheBloodTypeInThe(String quantity, String productFamily, String aboRh, String location, String days) {
        createProducts(quantity, productFamily, aboRh, location, days, InventoryStatus.AVAILABLE);
    }

    private void createProducts(String quantity, String productFamily, String aboRh, String location, String days, InventoryStatus status) {
        Stream.iterate(0, i -> i + 1)
            .limit(Integer.parseInt(quantity))
            .forEach(i -> this.createInventory(TestUtil.randomString(13), "E0869V00", productFamily, AboRhType.valueOf(aboRh), location, Integer.parseInt(days), status,"ACTIVE_DEFERRAL", null));
    }

    @Given("I have one product with {string}, {string} and {string} in {string} status")
    public void iHaveOneProductWithAndInStatus(String unitNumber, String productCode, String location, String status) {
        this.iHaveOneProductWithAndInStatusWithReason(unitNumber, productCode, location, status, "ACTIVE_DEFERRAL");
    }

    @Given("I have one product with {string}, {string} and {string} in {string} status with reason {string} and comments {string}")
    public void iHaveOneProductWithAndInStatus(String unitNumber, String productCode, String location, String status, String statusReason, String comments) {
        //Integer days = InventoryStatus.EXPIRED.equals(InventoryStatus.valueOf(status)) || InventoryStatus.DISCARDED.equals(InventoryStatus.valueOf(status))   ? -1 : 1;
        Integer days = "EXPIRED".equals(status)   ? -1 : 1;

        InventoryStatus inventoryStatus = "EXPIRED".equals(status) ? InventoryStatus.AVAILABLE : InventoryStatus.valueOf(status);

        createInventory(unitNumber, productCode, "PLASMA_TRANSFUSABLE", AboRhType.OP, location, days, inventoryStatus, statusReason, comments);
    }

    private void createInventory(String unitNumber, String productCode, String productFamily, AboRhType aboRhType, String location, Integer daysToExpire, InventoryStatus status, String statusReason, String comments) {
        this.createInventory(unitNumber, productCode, productFamily, aboRhType, location, daysToExpire, status, statusReason, comments, List.of());
    }

    private void createInventory(String unitNumber, String productCode, String productFamily, AboRhType aboRhType, String location, Integer daysToExpire, InventoryStatus status, String statusReason, String comments, List<Quarantine> specificQuarantines) {
        //List<Quarantine> quarantines = InventoryStatus.QUARANTINED.equals(status) && specificQuarantines.isEmpty() ? TestUtil.createQuarantines() : List.of();
        List<Quarantine> quarantines = specificQuarantines.isEmpty() ? TestUtil.createQuarantines() : List.of();
        if(!specificQuarantines.isEmpty()) {
            quarantines = specificQuarantines;
        }
        inventoryEntityRepository.save(InventoryEntity.builder()
            .id(UUID.randomUUID())
            .productFamily(productFamily)
            .aboRh(aboRhType)
            .location(location)
            .collectionDate(ZonedDateTime.now())
            .inventoryStatus(status)
            .expirationDate(LocalDateTime.now().plusDays(daysToExpire))
            .unitNumber(unitNumber)
            .weight(123)
            .isLicensed(true)
            .productCode(productCode)
            .statusReason(statusReason)
            .comments(comments)
            .shortDescription("Short description")
            .storageLocation("FREEZER 1, RACK 1, SHELF 1")
            .quarantines(quarantines)
            .isLabeled(true)
            .build()).block();

    }

    @When("I select {string} of the blood type {string}")
    public void iSelectOfTheOfTheBloodType(String productFamily, String aboRh) {
        inventoryCriteriaList.add(new AvailableInventoryCriteriaDTO(productFamily, AboRhCriteria.valueOf(aboRh)));
    }

    @When("I request {string} of the blood type {string} in the {string}")
    public void iRequestOfTheOfTheBloodType(String productFamily, String aboRh, String location) {
        getAvailableInventoryResponseDTOMonoResult = requester
            .route("getAvailableInventoryWithShortDatedProducts")
            .data(new GetAvailableInventoryCommandDTO(location, List.of(new AvailableInventoryCriteriaDTO(productFamily, AboRhCriteria.valueOf(aboRh)))))
            .retrieveMono(GetAvailableInventoryResponseDTO.class);
    }

    @When("I request to location {string}")
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
                assertThat(message.inventories().getFirst().shortDateProducts().size()).isEqualTo(Integer.parseInt(quantityShortDate));
                assertThat(message.inventories().getFirst().quantityAvailable()).isEqualTo(Integer.parseInt(quantityTotal));
                log.debug("Received message {}", message);
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
                        detail ->  assertThat(message.inventoryNotificationsDTO().getFirst().details().contains(detail)).isTrue());
                    assertThat(message.inventoryNotificationsDTO().size()).isEqualTo(1);
                }

                if (MessageType.INVENTORY_IS_EXPIRED.getCode().equals(errorCode)) {
                    assertThat(message.inventoryNotificationsDTO().getFirst().reason()).isEqualTo(reason);
                }

                log.debug("Received message from validate inventory {}", message);
            })
            .verifyComplete();
    }

    @And("I have one product with {string}, {string} and {string} in {string} status with quarantine reasons {string} and comments {string}")
    public void iHaveOneProductWithAndInStatusWithQuarantineReasonsAndComments(String unitNumber, String productCode, String location, String status, String quarantineReasons, String quarantineComments) {
        //Integer days = InventoryStatus.EXPIRED.equals(InventoryStatus.valueOf(status)) || InventoryStatus.DISCARDED.equals(InventoryStatus.valueOf(status))   ? -1 : 1;
        Integer days = InventoryStatus.DISCARDED.equals(InventoryStatus.valueOf(status))   ? -1 : 1;
        List<Quarantine> quarantines = Arrays.stream(quarantineReasons.split(",")).map(String::trim).map(reason -> new Quarantine(1L, reason, quarantineComments)).collect(Collectors.toList());
        createInventory(unitNumber, productCode, "PLASMA_TRANSFUSABLE", AboRhType.OP, location, days, InventoryStatus.valueOf(status), "ACTIVE_DEFERRAL", null, quarantines);
    }

    @And("I have one product with {string}, {string} and {string} in {string} status with reason {string}")
    public void iHaveOneProductWithAndInStatusWithReason(String unitNumber, String productCode, String location, String status, String reason) {
        //Integer days = InventoryStatus.EXPIRED.equals(InventoryStatus.valueOf(status)) || InventoryStatus.DISCARDED.equals(InventoryStatus.valueOf(status))   ? -1 : 1;
        Integer days =  "EXPIRED".equals(status) ? -1 : 1;
        InventoryStatus inventoryStatus = "EXPIRED".equals(status) ? InventoryStatus.AVAILABLE : InventoryStatus.valueOf(status);
        createInventory(unitNumber, productCode, "PLASMA_TRANSFUSABLE", AboRhType.OP, location, days, inventoryStatus, reason, null);
    }
}
