package com.arcone.biopro.distribution.inventory.verification.steps;

import com.arcone.biopro.distribution.inventory.adapter.in.socket.dto.*;
import com.arcone.biopro.distribution.inventory.application.dto.GetInventoryByUnitNumberAndProductInput;
import com.arcone.biopro.distribution.inventory.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhCriteria;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.MessageType;
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

    Flux<InventoryValidationResponseDTO> inventoryValidationResponseDTOFluxResult;

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

    @When("I request {string} in the {string}")
    public void iRequestValidateByUnitNumberWithInTheLocation(String unitNumber, String location) {
        inventoryValidationResponseDTOFluxResult = requester
            .route("validateInventoryByUnitNumber")
            .data(new InventoryValidationRequest(unitNumber, null, location))
            .retrieveFlux(InventoryValidationResponseDTO.class);
    }

    @When("I request a inventory with unit number {string}")
    public void iRequestGetInventoryByUnitNumber(String unitNumber) {
        getInventoryByUnitNumberFluxResult = requester
            .route("getInventoryByUnitNumber")
            .data(unitNumber)
            .retrieveFlux(InventoryOutput.class);
    }

    @When("I request a inventory with unit number {string} and product code {string}")
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
        String foundIt = row.get("Found It");

        Publisher<? extends InventoryValidationResponseDTO> publisher = inventoryValidationResponseDTOFluxResult != null ? inventoryValidationResponseDTOFluxResult : inventoryValidationResponseDTOMonoResult;

        if (inventoryValidationResponseDTOMonoResult != null) {
            assertToValidateInventory(inventoryValidationResponseDTOMonoResult.block(), row);
        } else if (inventoryValidationResponseDTOFluxResult != null) {
            List<InventoryValidationResponseDTO> list = inventoryValidationResponseDTOFluxResult.collectList().block();
            assert list != null;
            InventoryValidationResponseDTO inventoryValidationResponseDTO = list.stream()
                .filter(item -> unitNumber.equals(item.inventoryResponseDTO().unitNumber()) && productCode.equals(item.inventoryResponseDTO().productCode()))
                .findFirst().orElse(null);

            if ("False".equals(foundIt)) {
                assertNull(inventoryValidationResponseDTO);
            } else {
                assertToValidateInventory(inventoryValidationResponseDTO, row);
            }
        }
    }

    private void assertToValidateInventory(InventoryValidationResponseDTO message, Map<String, String> row) {

        String unitNumber = row.get("Unit Number");
        String productCode = row.get("Product Code");
        String temperatureCategory = row.get("Temperature Category");
        String location = row.get("Location");
        List<String> errorTypes = splitValues(row.get("RESPONSE ERROR"));

        if (Objects.nonNull(message.inventoryResponseDTO())) {
            assertThat(message.inventoryResponseDTO().unitNumber()).isEqualTo(unitNumber);
            assertThat(message.inventoryResponseDTO().productCode()).isEqualTo(productCode);
            assertThat(message.inventoryResponseDTO().temperatureCategory()).isEqualTo(temperatureCategory);

            if (!errorTypes.contains(MessageType.INVENTORY_NOT_FOUND_IN_LOCATION.name())) {
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

        if (errorTypes != null && !errorTypes.isEmpty()) {
            assertThat(message.inventoryNotificationsDTO().isEmpty()).isFalse();
            assertThat(message.inventoryNotificationsDTO().size()).isEqualTo(errorTypes.size());
            errorTypes.forEach(errorType -> assertNotifications(errorType, message.inventoryNotificationsDTO(), row));
        } else {
            assertThat(message.inventoryNotificationsDTO().isEmpty()).isTrue();
        }

        log.debug("Received message from validate inventory {}", message);

    }

    private static void assertNotifications(String errorType, List<InventoryNotificationDTO> inventoryNotificationDTOS, Map<String, String> row) {

        InventoryNotificationDTO inventoryNotificationDTO = inventoryNotificationDTOS.stream().filter(item -> item.errorName().equals(errorType)).findFirst().orElse(null);

        assertThat(inventoryNotificationDTO).isNotNull();

        String action = row.get("ACTION");
        String reason = row.get("REASON");
        String messageError = row.get("MESSAGE");
        String details = row.get("DETAILS");

        Integer errorCode =  MessageType.valueOf(errorType).getCode();

        assertThat(inventoryNotificationDTO.errorCode()).isEqualTo(errorCode);
        assertThat(inventoryNotificationDTO.errorName()).isEqualTo(errorType);
        if (Objects.nonNull(action)) {
            assertThat(inventoryNotificationDTO.action()).isEqualTo(action);

        }

        if (Objects.nonNull(messageError)) {
            assertThat(inventoryNotificationDTO.errorMessage()).isEqualTo(messageError);
        }

        if (Objects.nonNull(details) && MessageType.INVENTORY_IS_QUARANTINED.getCode().equals(errorCode)) {
            var detailsList = Arrays.stream(details.split(",")).map(String::toString).map(String::trim).toList();
            detailsList.forEach(
                detail -> assertThat(inventoryNotificationDTO.details().contains(detail)).isTrue());
        }

        if (Objects.nonNull(reason) && MessageType.INVENTORY_IS_EXPIRED.getCode().equals(errorCode)) {
            assertThat(inventoryNotificationDTO.reason()).isEqualTo(reason);
        }
        if (Objects.nonNull(reason) && MessageType.INVENTORY_IS_UNSUITABLE.getCode().equals(errorCode)) {
            assertThat(inventoryNotificationDTO.reason()).isEqualTo(reason);
        }
    }

    private List<String> splitValues(String values) {
        if (Objects.isNull(values)) {
            return List.of();
        }

        return Arrays.stream(values.split(",")).map(String::trim).toList();
    }

    @Then("I receive the following from get inventory by unit number:")
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

    @Then("I receive the following from get inventory by unit number and Product Code:")
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
