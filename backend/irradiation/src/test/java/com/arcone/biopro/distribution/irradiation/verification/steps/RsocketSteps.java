package com.arcone.biopro.distribution.irradiation.verification.steps;

import com.arcone.biopro.distribution.irradiation.adapter.in.socket.dto.*;
import com.arcone.biopro.distribution.irradiation.application.dto.GetInventoryByUnitNumberAndProductInput;
import com.arcone.biopro.distribution.irradiation.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.irradiation.domain.model.enumeration.AboRhCriteria;
import com.arcone.biopro.distribution.irradiation.domain.model.enumeration.MessageType;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

@Slf4j
public class RsocketSteps {

    @Autowired
    RSocketRequester.Builder builder;

    @Value("${spring.rsocket.server.port}")
    Integer port;

    Mono<GetAvailableInventoryResponseDTO> getAvailableInventoryResponseDTOMonoResult;

    GetAvailableInventoryResponseDTO getAvailableInventoryResponseDTOResult;

    Mono<InventoryValidationResponseDTO> inventoryValidationResponseDTOMonoResult;

    Flux<InventoryOutput> getInventoryByUnitNumberFluxResult;

    Mono<InventoryOutput> getInventoryByUnitNumberAndProductCodeMonoResult;


    private static RSocketRequester requester;

    List<AvailableInventoryCriteriaDTO> inventoryCriteriaList;

    @Before
    public void setupOnce() {
        requester = builder
            .tcp("localhost", port);

        inventoryCriteriaList = new ArrayList<>();
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

    @When("I request a irradiation with unit number {string}")
    public void iRequestGetInventoryByUnitNumber(String unitNumber) {
        getInventoryByUnitNumberFluxResult = requester
            .route("getInventoryByUnitNumber")
            .data(unitNumber)
            .retrieveFlux(InventoryOutput.class);
    }

    @When("I request a irradiation with unit number {string} and product code {string}")
    public void iRequestGetInventoryByUnitNumberAndProductCode(String unitNumber, String productCode) {
        getInventoryByUnitNumberAndProductCodeMonoResult = requester
            .route("getInventoryByUnitNumberAndProductCode")
            .data(new GetInventoryByUnitNumberAndProductInput(unitNumber, productCode))
            .retrieveMono(InventoryOutput.class);
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

    @Then("I receive the following:")
    public void iReceiveTheFollowing(DataTable dataTable) {
        var row = dataTable.asMaps(String.class, String.class).getFirst();
        String unitNumber = row.get("Unit Number");
        String productCode = row.get("Product Code");
        String temperatureCategory = row.get("Temperature Category");
        String location = row.get("Location");
        String errorType = row.get("RESPONSE ERROR");
        String action = row.get("ACTION");
        String reason = row.get("REASON");
        String messageError = row.get("MESSAGE");
        String details = row.get("DETAILS");
        Integer errorCode = Objects.isNull(errorType) ? null : MessageType.valueOf(errorType).getCode();

        StepVerifier
            .create(inventoryValidationResponseDTOMonoResult)
            .consumeNextWith(message -> {
                if (!MessageType.INVENTORY_NOT_EXIST.getCode().equals(errorCode)) {
                    assertThat(message.inventoryResponseDTO().unitNumber()).isEqualTo(unitNumber);
                    assertThat(message.inventoryResponseDTO().productCode()).isEqualTo(productCode);
                    assertThat(message.inventoryResponseDTO().temperatureCategory()).isEqualTo(temperatureCategory);

                    if (!MessageType.INVENTORY_NOT_FOUND_IN_LOCATION.getCode().equals(errorCode)) {
                        assertThat(message.inventoryResponseDTO().locationCode()).isEqualTo(location);
                        if (row.get("Collection Location") != null) {
                            assertThat(message.inventoryResponseDTO().collectionLocation()).isEqualTo(row.get("Collection Location"));
                        }
                    }

                    if (row.containsKey("Volumes") && row.get("Volumes") != null) {
                        var volumes = row.get("Volumes").split(",");
                        for(String volume: volumes) {
                            var volumeFields = volume.split("-");
                            assertTrue( message.inventoryResponseDTO().volumes().stream()
                                .filter(v -> v.type().equals(volumeFields[0].trim().toUpperCase()))
                                .findFirst()
                                .map(v -> Objects.equals(v.value(), Integer.parseInt(volumeFields[1].trim())))
                                .orElse(false));
                        }
                    }

                    if (row.get("Collection TimeZone") != null) {
                        assertThat(message.inventoryResponseDTO().collectionTimeZone()).isEqualTo(row.get("Collection TimeZone"));
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
                log.debug("Received message from validate irradiation {}", message);
            })
            .verifyComplete();
    }

    @Then("I receive the following from get irradiation by unit number:")
    public void iReceiveTheFollowingFromGetInventoryByUnitNumber(DataTable dataTable) {
        var row = dataTable.asMaps(String.class, String.class).getFirst();

        Publisher<? extends InventoryOutput> publisher = getInventoryByUnitNumberFluxResult != null ? getInventoryByUnitNumberFluxResult : getInventoryByUnitNumberAndProductCodeMonoResult;

        StepVerifier
            .create(publisher)
            .thenConsumeWhile(message -> {
                assertInventoryOutput(message, row);

                return true;
            }).verifyComplete();
    }

    @Then("I receive the following from get irradiation by unit number and Product Code:")
    public void iReceiveTheFollowingFromGetInventoryByUnitNumberAndProductCode(DataTable dataTable) {
        var row = dataTable.asMaps(String.class, String.class).getFirst();

        StepVerifier
            .create(getInventoryByUnitNumberAndProductCodeMonoResult)
            .assertNext(message -> {
                assertInventoryOutput(message, row);
            }).verifyComplete();
    }

    private static void assertInventoryOutput(InventoryOutput message, Map<String, String> row) {

        String unitNumber = row.get("Unit Number");
        String productCode = row.get("Product Code");
        assertThat(message.unitNumber()).isEqualTo(unitNumber);
        assertThat(productCode).contains(message.productCode());

        if (row.containsKey("Temperature Category")) {
            assertThat(message.temperatureCategory()).isEqualTo(row.get("Temperature Category"));

        }

        if (row.get("Location") != null) {
            assertThat(message.location()).isEqualTo(row.get("Location"));
        }

        if (row.get("Unsuitable Reason") != null) {
            assertThat(message.unsuitableReason()).isEqualTo(row.get("Unsuitable Reason"));
        }

        if (row.get("Discard Reason") != null) {
            assertThat(message.statusReason()).isEqualTo(row.get("Discard Reason"));
        }

        if (row.get("Quarantine Reasons") != null) {

            assertNotNull(message.quarantines());
            assertFalse(message.quarantines().isEmpty());

            message.quarantines().forEach(
                quarantine -> {
                    assertTrue(row.get("Quarantine Reasons").contains(quarantine.reason()));
                }
            );
        }

        if (row.get("Collection Location") != null) {
            assertThat(message.collectionLocation()).isEqualTo(row.get("Collection Location"));
        }

        if (row.containsKey("Volumes") && row.get("Volumes") != null) {
            var volumes = row.get("Volumes").split(",");
            for(String volume: volumes) {
                var volumeFields = volume.split("-");
                assertTrue( message.volumes().stream()
                    .filter(v -> v.type().equals(volumeFields[0].trim().toUpperCase()))
                    .findFirst()
                    .map(v -> Objects.equals(v.value(), Integer.parseInt(volumeFields[1].trim())))
                    .orElse(false));
            }
        }

        if (row.get("Collection TimeZone") != null) {
            assertThat(message.collectionTimeZone()).isEqualTo(row.get("Collection TimeZone"));
        }

        if(row.containsKey("Expired")) {
            assertThat(message.expired()).isEqualTo(Boolean.valueOf(row.get("Expired").toLowerCase()));
        }
    }
}
