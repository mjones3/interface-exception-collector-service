package com.arcone.biopro.distribution.shippingservice.infrastructure.controller;

import com.arcone.biopro.distribution.shippingservice.infrastructure.controller.dto.InventoryMockData;
import com.arcone.biopro.distribution.shippingservice.infrastructure.controller.dto.InventoryNotificationDTO;
import com.arcone.biopro.distribution.shippingservice.infrastructure.controller.dto.InventoryResponseDTO;
import com.arcone.biopro.distribution.shippingservice.infrastructure.controller.dto.InventoryValidationRequest;
import com.arcone.biopro.distribution.shippingservice.infrastructure.controller.dto.InventoryValidationResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class InventoryMockController {
    private final ObjectMapper objectMapper;
    private List<InventoryResponseDTO> inventoryResponseDTOList;

    @MessageMapping("validateInventory")
    public Mono<InventoryValidationResponseDTO> validateInventory(@Payload InventoryValidationRequest request) {
        log.info("Checking inventory {} , {} , {} ",request.unitNumber(), request.productCode() , request.locationCode());

        if(inventoryResponseDTOList == null){
            initInventoryMockList();
        }

            /*| Expired            | W036898786756  | E0701V00    Expired error message     |
            | Discarded            | =W03689878675700 | =<E0713V00  Discarded error message   |
            | Quarantined          | W036898786758    | E0707V00    Quarantined error message |
            | Non existent         | =W03689878675900 | =<E0701V00  Non existent error        |
            | Different Location   | =W03689878676300 | =<E0703V00  Product not found error   |*/


        switch (request.unitNumber()) {
            case "W036898786756":
                return Mono.just(InventoryValidationResponseDTO
                .builder()
                .inventoryNotificationDTO(InventoryNotificationDTO
                    .builder()
                    .errorCode(2)
                    .errorMessage("inventory-expired.label")
                    .build())
                .build());
            case "W036898786757":
                return Mono.just(InventoryValidationResponseDTO
                    .builder()
                    .inventoryNotificationDTO(InventoryNotificationDTO
                        .builder()
                        .errorCode(3)
                        .errorMessage("inventory-discarded.label")
                        .build())
                    .build());
            case "W036898786758":
                return Mono.just(InventoryValidationResponseDTO
                    .builder()
                    .inventoryNotificationDTO(InventoryNotificationDTO
                        .builder()
                        .errorCode(4)
                        .errorMessage("inventory-quarantined.label")
                        .build())
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
                if(inventoryResponse.isPresent()){

                    return Mono.just(InventoryValidationResponseDTO
                        .builder()
                        .inventoryResponseDTO(inventoryResponse.get())
                        .build());
                }else{
                    return Mono.just(InventoryValidationResponseDTO
                        .builder()
                        .inventoryNotificationDTO(InventoryNotificationDTO
                            .builder()
                            .errorCode(1)
                            .errorMessage("inventory-not-found.label")
                            .build())
                        .build());
                }

        }
    }

    private void initInventoryMockList() {
        inventoryResponseDTOList = new ArrayList<>();
        try {
            var fileInputStream = new ClassPathResource("mock/inventory/inventory-mock-data.json").getInputStream();
            var mockData = objectMapper.readValue(fileInputStream, InventoryMockData.class);
            inventoryResponseDTOList = mockData.data();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
