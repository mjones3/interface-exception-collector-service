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
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Slf4j
@Profile("inventory-mock")
public class InventoryMockController {
    private final ObjectMapper objectMapper;
    private List<InventoryResponseDTO> inventoryResponseDTOList;
    private List<InventoryResponseDTO> inventoryByUnitResponseDTOList;
    private static final String DISCARD_COMMENTS_250_CHARS = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec.";

    @MessageMapping("validateInventory")
    public Mono<InventoryValidationResponseDTO> validateInventory(@Payload InventoryValidationRequest request) {
        log.info("Checking inventory {} , {} , {} ", request.unitNumber(), request.productCode(), request.locationCode());

        if (inventoryResponseDTOList == null) {
            inventoryResponseDTOList = initInventoryMockList("mock/inventory/inventory-mock-data.json");
        }

            /*| Expired            | W036898786756  | E0701V00    Expired error message     |
            | Discarded            | =W03689878675700 | =<E0713V00  Discarded error message   |
            | Discarded            | W036898786759 | E0713V00  Discarded error message plus discard comments  |
            | Quarantined          | W036898786758    | E0707V00    Quarantined error message |
            | Non existent         | =W03689878675900 | =<E0701V00  Non existent error        |
            | Different Location   | =W03689878676300 | =<E0703V00  Product not found error   |
            | Already Shipped      | W036898786700    | E0707V00
            */


        switch (request.unitNumber()) {
            case "W036898786756":
                return Mono.just(InventoryValidationResponseDTO
                    .builder()
                    .inventoryResponseDTO(InventoryResponseDTO
                        .builder()
                        .productFamily("PLASMA_TRANSFUSABLE")
                        .id(UUID.randomUUID())
                        .aboRh("AB")
                        .locationCode("123456789")
                        .productCode("E0701V00")
                        .collectionDate(ZonedDateTime.now())
                        .unitNumber("W036898786756")
                        .expirationDate(LocalDateTime.now())
                        .temperatureCategory("FROZEN")
                        .productDescription("PRODUCT_DESCRIPTION")
                        .status("EXPIRED")
                        .build())
                    .inventoryNotificationsDTO(List.of(InventoryNotificationDTO
                        .builder()
                        .errorName("INVENTORY_IS_EXPIRED")
                        .errorType("INFO")
                        .errorCode(2)
                        .errorMessage(ShipmentServiceMessages.INVENTORY_EXPIRED_ERROR)
                        .action("TRIGGER_DISCARD")
                        .reason("EXPIRED")
                        .build())).build()
                );
            case "W036898786757":
                return Mono.just(InventoryValidationResponseDTO
                    .builder()
                    .inventoryResponseDTO(InventoryResponseDTO
                        .builder()
                        .productFamily("PLASMA_TRANSFUSABLE")
                        .id(UUID.randomUUID())
                        .aboRh("AB")
                        .locationCode("123456789")
                        .productCode("E0713V00")
                        .collectionDate(ZonedDateTime.now())
                        .unitNumber("W036898786757")
                        .expirationDate(LocalDateTime.now())
                        .productDescription("PRODUCT_DESCRIPTION")
                        .temperatureCategory("FROZEN")
                        .status("DISCARDED")
                        .build())
                    .inventoryNotificationsDTO(List.of(InventoryNotificationDTO
                        .builder()
                        .errorName("INVENTORY_IS_DISCARDED")
                        .errorCode(3)
                        .errorType("INFO")
                        .errorMessage(ShipmentServiceMessages.INVENTORY_DISCARDED_ERROR)
                        .build()))
                    .build());
            case "W036898786759":
                return Mono.just(InventoryValidationResponseDTO
                    .builder()
                    .inventoryResponseDTO(InventoryResponseDTO
                        .builder()
                        .productFamily("PLASMA_TRANSFUSABLE")
                        .id(UUID.randomUUID())
                        .aboRh("AB")
                        .locationCode("123456789")
                        .productCode("E0713V00")
                        .collectionDate(ZonedDateTime.now())
                        .unitNumber("W036898786759")
                        .expirationDate(LocalDateTime.now())
                        .temperatureCategory("FROZEN")
                        .productDescription("PRODUCT_DESCRIPTION")
                        .status("DISCARDED")
                        .build())
                    .inventoryNotificationsDTO(List.of(InventoryNotificationDTO
                        .builder()
                        .errorName("INVENTORY_IS_DISCARDED")
                        .errorCode(3)
                        .errorType("INFO")
                        .errorMessage(ShipmentServiceMessages.INVENTORY_DISCARDED_ERROR + DISCARD_COMMENTS_250_CHARS)
                        .build()))
                    .build());
            case "W036898786758":


                return Mono.just(InventoryValidationResponseDTO
                    .builder()
                    .inventoryResponseDTO(InventoryResponseDTO
                        .builder()
                        .productFamily("PLASMA_TRANSFUSABLE")
                        .id(UUID.randomUUID())
                        .aboRh("AB")
                        .locationCode("123456789")
                        .productCode("E0707V00")
                        .collectionDate(ZonedDateTime.now())
                        .temperatureCategory("FROZEN")
                        .unitNumber("W036898786758")
                        .expirationDate(LocalDateTime.now())
                        .productDescription("PRODUCT_DESCRIPTION")
                        .status("QUARANTINED")
                        .build())
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
                    .build());
            case "W036898786700":
                return Mono.just(InventoryValidationResponseDTO
                    .builder()
                    .inventoryResponseDTO(InventoryResponseDTO
                        .builder()
                        .productFamily("PLASMA_TRANSFUSABLE")
                        .id(UUID.randomUUID())
                        .aboRh("AB")
                        .locationCode("123456789")
                        .productCode("E0707V00")
                        .temperatureCategory("FROZEN")
                        .collectionDate(ZonedDateTime.now())
                        .unitNumber("W036898786700")
                        .expirationDate(LocalDateTime.now())
                        .productDescription("PRODUCT_DESCRIPTION")
                        .status("SHIPPED")
                        .build())
                    .inventoryNotificationsDTO(List.of(InventoryNotificationDTO
                        .builder()
                        .errorType("INFO")
                        .errorName("INVENTORY_IS_SHIPPED")
                        .errorCode(4)
                        .errorMessage(ShipmentServiceMessages.INVENTORY_NOT_FOUND_ERROR)
                        .build()))
                    .build());
            case "W036898786812":
                return Mono.error(new RuntimeException("Testing Exception Handlers"));
            default:
                var inventoryResponse = inventoryResponseDTOList.stream().filter(inventory -> {
                        return inventory.unitNumber().equals(request.unitNumber())
                            && inventory.productCode().equals(request.productCode())
                            && inventory.locationCode().equals(request.locationCode());
                    }
                ).findAny();
                return inventoryResponse.map(inventoryResponseDTO -> Mono.just(InventoryValidationResponseDTO
                    .builder()
                    .inventoryResponseDTO(inventoryResponseDTO)
                    .build())).orElseGet(() -> Mono.just(InventoryValidationResponseDTO
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

    private List<InventoryResponseDTO> initInventoryMockList(String mockFile) {
        try {
            var fileInputStream = new ClassPathResource(mockFile).getInputStream();
            var mockData = objectMapper.readValue(fileInputStream, InventoryMockData.class);
            return mockData.data();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @MessageMapping("validateInventoryByUnitNumber")
    public Flux<InventoryValidationResponseDTO> validateInventoryByUnitNumber(@Payload InventoryValidationByUnitNumberRequest request) {
        log.debug("Checking inventory by Unit Number {} , {} ", request.unitNumber(), request.locationCode());

        if (inventoryByUnitResponseDTOList == null) {
            inventoryByUnitResponseDTOList = initInventoryMockList("mock/inventory/inventory-by-unit-mock-data.json");
        }

        return Flux.fromStream(inventoryByUnitResponseDTOList.stream().filter(inventory -> {
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
            default -> InventoryValidationResponseDTO
                .builder()
                .inventoryResponseDTO(inventoryResponseDTO)
                .build();
        };
    }

}
