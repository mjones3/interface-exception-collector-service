package com.arcone.biopro.distribution.shipping.infrastructure.controller;

import com.arcone.biopro.distribution.shipping.application.util.ShipmentServiceMessages;
import com.arcone.biopro.distribution.shipping.infrastructure.controller.dto.InventoryMockData;
import com.arcone.biopro.distribution.shipping.infrastructure.controller.dto.InventoryNotificationDTO;
import com.arcone.biopro.distribution.shipping.infrastructure.controller.dto.InventoryResponseDTO;
import com.arcone.biopro.distribution.shipping.infrastructure.controller.dto.InventoryValidationByUnitNumberRequest;
import com.arcone.biopro.distribution.shipping.infrastructure.controller.dto.InventoryValidationRequest;
import com.arcone.biopro.distribution.shipping.infrastructure.controller.dto.InventoryValidationResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
@Profile("inventory-mock")
public class InventoryMockController {
    private final ObjectMapper objectMapper;
    private List<InventoryResponseDTO> inventoryResponseDTOList;
    private static final String DISCARD_COMMENTS_250_CHARS = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec.";

    @MessageMapping("validateInventory")
    public Mono<InventoryValidationResponseDTO> validateInventory(@Payload InventoryValidationRequest request) {
        log.info("Checking inventory {} , {} , {} ", request.unitNumber(), request.productCode(), request.locationCode());

        if (inventoryResponseDTOList == null) {
            inventoryResponseDTOList = initInventoryMockList();
        }
        switch (request.unitNumber()) {
            case "W036898786812":
                return Mono.error(new RuntimeException("Testing Exception Handlers"));
            default:
                var inventoryResponse = inventoryResponseDTOList.stream().filter(inventory -> {
                        return inventory.unitNumber().equals(request.unitNumber())
                            && inventory.productCode().equals(request.productCode())
                            && inventory.locationCode().equals(request.locationCode());
                    }
                ).findAny();
                return inventoryResponse.map(inventoryResponseDTO -> Mono.just(this.buildResponseDtoByStatus(inventoryResponseDTO)))
                    .orElseGet(() -> Mono.just(InventoryValidationResponseDTO
                    .builder()
                    .inventoryNotificationsDTO(List.of(InventoryNotificationDTO
                        .builder()
                        .errorName("INVENTORY_NOT_FOUND_IN_LOCATION")
                        .errorType("WARN")
                        .errorCode(1)
                        .errorMessage(ShipmentServiceMessages.INVENTORY_NOT_FOUND_ERROR)
                        .build()))
                    .build()));

        }
    }

    private List<InventoryResponseDTO> initInventoryMockList() {
        try {
            var fileInputStream = new ClassPathResource("mock/inventory/inventory-mock-data.json").getInputStream();
            var mockData = objectMapper.readValue(fileInputStream, InventoryMockData.class);
            return mockData.data();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @MessageMapping("validateInventoryByUnitNumber")
    public Flux<InventoryValidationResponseDTO> validateInventoryByUnitNumber(@Payload InventoryValidationByUnitNumberRequest request) {
        log.debug("Checking inventory by Unit Number {} , {} ", request.unitNumber(), request.locationCode());

        if (inventoryResponseDTOList == null) {
            inventoryResponseDTOList =  initInventoryMockList();
        }

        return Flux.fromStream(inventoryResponseDTOList.stream().filter(inventory -> {
                return inventory.unitNumber().equals(request.unitNumber())
                    && inventory.locationCode().equals(request.locationCode());
            }).map(this::buildResponseDtoByStatus))
            .switchIfEmpty(Flux.just(InventoryValidationResponseDTO
                .builder()
                .inventoryNotificationsDTO(List.of(InventoryNotificationDTO
                    .builder()
                    .errorName("INVENTORY_NOT_FOUND_IN_LOCATION")
                    .errorType("WARN")
                    .errorCode(1)
                    .errorMessage(ShipmentServiceMessages.INVENTORY_NOT_FOUND_ERROR)
                    .build()))
                .build()));
    }


    private InventoryValidationResponseDTO buildResponseDtoByStatus(InventoryResponseDTO inventoryResponseDTO){

        return switch (inventoryResponseDTO.status()) {
            case "EXPIRED" -> InventoryValidationResponseDTO
                .builder()
                .inventoryResponseDTO(inventoryResponseDTO)
                .inventoryNotificationsDTO(List.of(InventoryNotificationDTO
                    .builder()
                    .errorName("INVENTORY_IS_EXPIRED")
                    .errorType("INFO")
                    .errorCode(2)
                    .errorMessage(ShipmentServiceMessages.INVENTORY_EXPIRED_ERROR)
                    .action("TRIGGER_DISCARD")
                    .reason("EXPIRED")
                    .build())).build();
            case "DISCARDED" -> InventoryValidationResponseDTO
                .builder()
                .inventoryResponseDTO(inventoryResponseDTO)
                .inventoryNotificationsDTO(List.of(InventoryNotificationDTO
                    .builder()
                    .errorName("INVENTORY_IS_DISCARDED")
                    .errorCode(3)
                    .errorType("INFO")
                    .errorMessage(ShipmentServiceMessages.INVENTORY_DISCARDED_ERROR + DISCARD_COMMENTS_250_CHARS)
                    .build()))
                .build();
            case "QUARANTINED" -> InventoryValidationResponseDTO
                .builder()
                .inventoryResponseDTO(inventoryResponseDTO)
                .inventoryNotificationsDTO(List.of(InventoryNotificationDTO
                    .builder()
                    .errorType("INFO")
                    .errorName("INVENTORY_IS_QUARANTINED")
                    .errorCode(4)
                    .errorMessage(ShipmentServiceMessages.INVENTORY_QUARANTINED_ERROR)
                    .details(List.of("ABS Positive", "BCA Unit Needed", "CCP Eligible", "Failed Visual Inspection"
                        , "Hold Until Expiration", "In Process Hold", "Pending Further Review Inspection",
                        "Save Plasma for CTS", "Other", "Under Investigation"))
                    .build()))
                .build();
            case "SHIPPED" -> InventoryValidationResponseDTO
                .builder()
                .inventoryResponseDTO(inventoryResponseDTO)
                .inventoryNotificationsDTO(List.of(InventoryNotificationDTO
                    .builder()
                    .errorType("INFO")
                    .errorName("INVENTORY_IS_SHIPPED")
                    .errorCode(4)
                    .errorMessage(ShipmentServiceMessages.INVENTORY_NOT_FOUND_ERROR)
                    .build()))
                .build();
            case "LABELED" -> InventoryValidationResponseDTO
                .builder()
                .inventoryResponseDTO(inventoryResponseDTO)
                .inventoryNotificationsDTO(List.of(InventoryNotificationDTO
                    .builder()
                    .errorType("WARN")
                    .errorName("INVENTORY_IS_LABELED")
                    .errorCode(5)
                    .errorMessage(ShipmentServiceMessages.INVENTORY_LABELED_ERROR)
                    .build()))
                .build();
            case "UNLABELED" -> InventoryValidationResponseDTO
                .builder()
                .inventoryResponseDTO(inventoryResponseDTO)
                .inventoryNotificationsDTO(List.of(InventoryNotificationDTO
                    .builder()
                    .errorType("WARN")
                    .errorName("INVENTORY_IS_UNLABELED")
                    .errorCode(6)
                    .errorMessage(ShipmentServiceMessages.INVENTORY_UNLABELED_ERROR)
                    .build()))
                .build();
            default -> InventoryValidationResponseDTO
                .builder()
                .inventoryResponseDTO(inventoryResponseDTO)
                .build();
        };
    }

}
