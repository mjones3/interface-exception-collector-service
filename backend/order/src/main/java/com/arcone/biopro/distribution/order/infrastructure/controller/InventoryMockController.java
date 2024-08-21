package com.arcone.biopro.distribution.order.infrastructure.controller;

import com.arcone.biopro.distribution.order.infrastructure.dto.GetAvailableInventoryCommandDTO;
import com.arcone.biopro.distribution.order.infrastructure.dto.GetAvailableInventoryDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Controller
@RequiredArgsConstructor
@Slf4j
@Profile("inventory-mock")
public class InventoryMockController {

    private GetAvailableInventoryDTO getAvailableInventoryDTO;
    private final ObjectMapper objectMapper;

    @MessageMapping("getAvailableInventoryWithShortDatedProducts")
    public Mono<GetAvailableInventoryDTO> getAvailableInventoryWithShortDatedProducts(@Payload GetAvailableInventoryCommandDTO getAllAvailableRequestDTO) {
        log.info("getAvailableInventoryWithShortDatedProducts to get all available inventories with request: {}", getAllAvailableRequestDTO.toString());
        return Mono.just(getAvailableInventoryDTO);
    }

    @PostConstruct
    private void postConstruct() throws IOException {
        try (var inputStream = new ClassPathResource("mock/inventory/inventory-available-mock-data.json").getInputStream()) {
            this.getAvailableInventoryDTO =  objectMapper.readValue(inputStream, GetAvailableInventoryDTO.class);

        }
    }
}
