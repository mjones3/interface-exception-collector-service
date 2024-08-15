package com.arcone.biopro.distribution.inventory.verification.steps;

import com.arcone.biopro.distribution.inventory.adapter.in.socket.dto.AvailableInventoryCriteriaDTO;
import com.arcone.biopro.distribution.inventory.adapter.in.socket.dto.GetAvailableInventoryCommandDTO;
import com.arcone.biopro.distribution.inventory.adapter.in.socket.dto.GetAvailableInventoryResponseDTO;
import com.arcone.biopro.distribution.inventory.adapter.in.socket.dto.Inventory;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhCriteria;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.ProductFamily;
import com.arcone.biopro.distribution.inventory.infrastructure.persistence.InventoryEntity;
import com.arcone.biopro.distribution.inventory.infrastructure.persistence.InventoryEntityRepository;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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

    Mono<GetAvailableInventoryResponseDTO> result;

    GetAvailableInventoryResponseDTO getAvailableInventoryResponseDTOResult;

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
        Stream.iterate(0, i -> i + 1)
            .limit(Integer.parseInt(quantity))
            .forEach(i -> this.createInventory(ProductFamily.valueOf(productFamily), AboRhType.valueOf(aboRh), location, Integer.parseInt(days)));
    }

    private void createInventory(ProductFamily productFamily, AboRhType aboRhType, String location, Integer daysToExpire) {
        inventoryEntityRepository.save(InventoryEntity.builder()
                .id(UUID.randomUUID())
                .productFamily(productFamily)
                .aboRh(aboRhType)
                .location(location)
                .collectionDate(ZonedDateTime.now().toString())
                .inventoryStatus(InventoryStatus.AVAILABLE)
                .expirationDate(LocalDateTime.now().plusDays(daysToExpire))
                .unitNumber(randomString(13))
                .productCode("E0869V00")
                .shortDescription("Short description")
            .build()).block();

    }

    public String randomString(int length) {
        return "W" + RandomStringUtils.random(length-1, false, true);
    }

    @When("I select {string} of the blood type {string}")
    public void iSelectOfTheOfTheBloodType(String productFamily, String aboRh) {
        inventoryCriteriaList.add(new AvailableInventoryCriteriaDTO(ProductFamily.valueOf(productFamily), AboRhCriteria.valueOf(aboRh)));
    }

    @When("I request {string} of the blood type {string} in the {string}")
    public void iRequestOfTheOfTheBloodType(String productFamily, String aboRh, String location) {
        result = requester
            .route("getAvailableInventoryWithShortDatedProducts")
            .data(new GetAvailableInventoryCommandDTO(location, List.of(new AvailableInventoryCriteriaDTO(ProductFamily.valueOf(productFamily), AboRhCriteria.valueOf(aboRh)))))
            .retrieveMono(GetAvailableInventoryResponseDTO.class);
    }

    @When("I request to location {string}")
    public void iRequestToLocation(String location) {
        result = requester
            .route("getAvailableInventoryWithShortDatedProducts")
            .data(new GetAvailableInventoryCommandDTO(location, inventoryCriteriaList))
            .retrieveMono(GetAvailableInventoryResponseDTO.class);

        getAvailableInventoryResponseDTOResult = result.block();
    }

    @Then("I receive {string} of total products and {string} of short date")
    public void iReceive(String quantityTotal, String quantityShortDate) {
        StepVerifier
            .create(result)
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
                inv.productFamily().equals(ProductFamily.valueOf(productFamily)) && inv.aboRh().equals(AboRhCriteria.valueOf(aboRhCriteria)))
            .findFirst()
            .orElse(null);

        assertThat(inventory).isNotNull();
        assertThat(inventory.quantityAvailable()).isEqualTo(Integer.parseInt(quantityAvailable));
        assertThat(inventory.shortDateProducts().size()).isEqualTo(Integer.parseInt(quantityShortDate));
    }
}
