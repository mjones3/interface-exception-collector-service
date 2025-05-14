package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.controller;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.util.RecoveredPlasmaShippingServiceMessages;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.controller.dto.InventoryMockData;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.controller.dto.InventoryNotificationDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.controller.dto.InventoryResponseDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.controller.dto.InventoryValidationRequest;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.controller.dto.InventoryValidationResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
            initInventoryMockList();
        }

        var inventoryResponse = inventoryResponseDTOList.stream().filter(inventory -> {
                return inventory.unitNumber().equals(request.unitNumber())
                    && inventory.productCode().equals(request.productCode())
                    && inventory.locationCode().equals(request.locationCode());
            }
        ).findAny();
        if (inventoryResponse.isPresent()) {

            return defineInventoryResponse(inventoryResponse.get());
        } else {
            return Mono.just(InventoryValidationResponseDTO
                .builder()
                .inventoryNotificationsDTO(List.of(InventoryNotificationDTO
                    .builder()
                    .errorName("INVENTORY_NOT_FOUND_IN_LOCATION")
                    .errorType("WARN")
                    .errorCode(1)
                    .errorMessage(RecoveredPlasmaShippingServiceMessages.INVENTORY_NOT_FOUND_ERROR)
                    .build()))
                .build());
        }
    }


    private Mono<InventoryValidationResponseDTO> defineInventoryResponse(InventoryResponseDTO inventoryResponseDTO) {

        return switch (inventoryResponseDTO.status()) {
            case "EXPIRED" -> Mono.just(InventoryValidationResponseDTO
                .builder()
                .inventoryResponseDTO(InventoryResponseDTO
                    .builder()
                    .productFamily(inventoryResponseDTO.productFamily())
                    .id(UUID.randomUUID())
                    .aboRh(inventoryResponseDTO.aboRh())
                    .locationCode(inventoryResponseDTO.locationCode())
                    .productCode(inventoryResponseDTO.productCode())
                    .collectionDate(ZonedDateTime.now())
                    .unitNumber(inventoryResponseDTO.unitNumber())
                    .expirationDate(LocalDateTime.now())
                    .productDescription(inventoryResponseDTO.productDescription())
                    .build())
                .inventoryNotificationsDTO(List.of(InventoryNotificationDTO
                    .builder()
                    .errorName("INVENTORY_IS_EXPIRED")
                    .errorType("INFO")
                    .errorCode(2)
                    .errorMessage(RecoveredPlasmaShippingServiceMessages.INVENTORY_EXPIRED_ERROR)
                    .action("TRIGGER_DISCARD")
                    .reason("EXPIRED")
                    .build())).build()
            );
            case "DISCARDED" -> Mono.just(InventoryValidationResponseDTO
                .builder()
                .inventoryResponseDTO(InventoryResponseDTO
                    .builder()
                    .productFamily(inventoryResponseDTO.productFamily())
                    .id(UUID.randomUUID())
                    .aboRh(inventoryResponseDTO.aboRh())
                    .locationCode(inventoryResponseDTO.locationCode())
                    .productCode(inventoryResponseDTO.productCode())
                    .collectionDate(ZonedDateTime.now())
                    .unitNumber(inventoryResponseDTO.unitNumber())
                    .expirationDate(LocalDateTime.now())
                    .productDescription(inventoryResponseDTO.productDescription())
                    .build())
                .inventoryNotificationsDTO(List.of(InventoryNotificationDTO
                    .builder()
                    .errorName("INVENTORY_IS_DISCARDED")
                    .errorCode(3)
                    .errorType("INFO")
                    .errorMessage(RecoveredPlasmaShippingServiceMessages.INVENTORY_DISCARDED_ERROR)
                    .build()))
                .build());
            case "DISCARDED_COMMENTS" -> Mono.just(InventoryValidationResponseDTO
                .builder()
                .inventoryResponseDTO(InventoryResponseDTO
                    .builder()
                    .productFamily(inventoryResponseDTO.productFamily())
                    .id(UUID.randomUUID())
                    .aboRh(inventoryResponseDTO.aboRh())
                    .locationCode(inventoryResponseDTO.locationCode())
                    .productCode(inventoryResponseDTO.productCode())
                    .collectionDate(ZonedDateTime.now())
                    .unitNumber(inventoryResponseDTO.unitNumber())
                    .expirationDate(LocalDateTime.now())
                    .productDescription(inventoryResponseDTO.productDescription())
                    .build())
                .inventoryNotificationsDTO(List.of(InventoryNotificationDTO
                    .builder()
                    .errorName("INVENTORY_IS_DISCARDED")
                    .errorCode(3)
                    .errorType("INFO")
                    .errorMessage(RecoveredPlasmaShippingServiceMessages.INVENTORY_DISCARDED_ERROR + DISCARD_COMMENTS_250_CHARS)
                    .build()))
                .build());
            case "QUARANTINED" -> Mono.just(InventoryValidationResponseDTO
                .builder()
                .inventoryResponseDTO(InventoryResponseDTO
                    .builder()
                    .productFamily(inventoryResponseDTO.productFamily())
                    .id(UUID.randomUUID())
                    .aboRh(inventoryResponseDTO.aboRh())
                    .locationCode(inventoryResponseDTO.locationCode())
                    .productCode(inventoryResponseDTO.productCode())
                    .collectionDate(ZonedDateTime.now())
                    .unitNumber(inventoryResponseDTO.unitNumber())
                    .expirationDate(LocalDateTime.now())
                    .productDescription(inventoryResponseDTO.productDescription())
                    .build())
                .inventoryNotificationsDTO(List.of(InventoryNotificationDTO
                    .builder()
                    .errorType("INFO")
                    .errorName("INVENTORY_IS_QUARANTINED")
                    .errorCode(4)
                    .errorMessage(RecoveredPlasmaShippingServiceMessages.INVENTORY_QUARANTINED_ERROR)
                    .details(List.of("ABS Positive", "BCA Unit Needed", "CCP Eligible", "Failed Visual Inspection"
                        , "Hold Until Expiration", "In Process Hold", "Pending Further Review Inspection",
                        "Save Plasma for CTS", "Other", "Under Investigation"))
                    .build()))
                .build());
            case "SHIPPED" -> Mono.just(InventoryValidationResponseDTO
                .builder()
                .inventoryResponseDTO(InventoryResponseDTO
                    .builder()
                    .productFamily(inventoryResponseDTO.productFamily())
                    .id(UUID.randomUUID())
                    .aboRh(inventoryResponseDTO.aboRh())
                    .locationCode(inventoryResponseDTO.locationCode())
                    .productCode(inventoryResponseDTO.productCode())
                    .collectionDate(ZonedDateTime.now())
                    .unitNumber(inventoryResponseDTO.unitNumber())
                    .expirationDate(LocalDateTime.now())
                    .productDescription(inventoryResponseDTO.productDescription())
                    .build())
                .inventoryNotificationsDTO(List.of(InventoryNotificationDTO
                    .builder()
                    .errorType("WARN")
                    .errorName("INVENTORY_IS_SHIPPED")
                    .errorCode(4)
                    .errorMessage(RecoveredPlasmaShippingServiceMessages.INVENTORY_SHIPPED_ERROR)
                    .build()))
                .build());
            case "SYSTEM_DOW" -> Mono.error(new RuntimeException("Testing Exception Handlers"));
            default -> Mono.just(InventoryValidationResponseDTO
                .builder()
                .inventoryResponseDTO(inventoryResponseDTO)
                .build());
        };

    }

    private void initInventoryMockList() {
        inventoryResponseDTOList = new ArrayList<>();
        try {
            var fileInputStream = new ClassPathResource("mock/inventory/inventory-mock-data.json").getInputStream();
            var mockData = objectMapper.readValue(fileInputStream, InventoryMockData.class);
            inventoryResponseDTOList = mockData.data();
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
