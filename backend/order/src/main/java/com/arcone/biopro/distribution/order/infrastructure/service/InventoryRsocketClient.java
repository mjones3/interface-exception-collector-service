package com.arcone.biopro.distribution.order.infrastructure.service;

import com.arcone.biopro.distribution.order.domain.model.AvailableInventory;
import com.arcone.biopro.distribution.order.domain.model.GeneratePickListCommand;
import com.arcone.biopro.distribution.order.domain.model.ShortDateProduct;
import com.arcone.biopro.distribution.order.domain.service.InventoryService;
import com.arcone.biopro.distribution.order.infrastructure.dto.AvailableInventoryCriteriaDTO;
import com.arcone.biopro.distribution.order.infrastructure.dto.AvailableInventoryDTO;
import com.arcone.biopro.distribution.order.infrastructure.dto.GetAvailableInventoryDTO;
import com.arcone.biopro.distribution.order.infrastructure.dto.GetAvailableInventoryCommandDTO;
import com.arcone.biopro.distribution.order.infrastructure.exception.InventoryServiceNotAvailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

import static java.util.Optional.ofNullable;

@Service
@Slf4j
public class InventoryRsocketClient implements InventoryService {

    private final RSocketRequester rSocketRequester;

    public InventoryRsocketClient(@Qualifier("inventory") RSocketRequester rSocketRequester) {
        this.rSocketRequester = rSocketRequester;
    }

    public Mono<GetAvailableInventoryDTO> getAvailableInventoryWithShortDatedProducts(GetAvailableInventoryCommandDTO commandDTO){
        return rSocketRequester
            .route("getAvailableInventoryWithShortDatedProducts")
            .data(commandDTO)
            .retrieveMono(GetAvailableInventoryDTO.class)
            .log();
    }

    @MessageExceptionHandler(RuntimeException.class)
    public Mono<RuntimeException> exceptionHandler(RuntimeException e) {
        log.error(e.getMessage());
        return Mono.error(new InventoryServiceNotAvailableException("Inventory service not available"));
    }

    @Override
    public Flux<AvailableInventory> getAvailableInventories(GeneratePickListCommand generatePickListCommand) {
        return getAvailableInventoryWithShortDatedProducts(GetAvailableInventoryCommandDTO
            .builder()
            .locationCode(generatePickListCommand.getLocationCode())
            .availableInventoryCriteriaDTOS(generatePickListCommand.getProductCriteria()
                .stream()
                .map(productCriteria -> AvailableInventoryCriteriaDTO
                    .builder()
                    .bloodType(productCriteria.getBloodType())
                    .productFamily(productCriteria.getProductFamily())
                    .build())
                .toList())
            .build())
            .flatMapMany(this::mapToDomain);
    }

    private Flux<AvailableInventory> mapToDomain(GetAvailableInventoryDTO availableInventoryDTO) {
        return Flux.fromIterable(availableInventoryDTO.inventories().stream()
            .map(dto -> new AvailableInventory(dto.productFamily(), dto.aboRh(), dto.quantityAvailable(),mapShortDateList(dto)))
            .toList());
    }

    private List<ShortDateProduct> mapShortDateList(AvailableInventoryDTO dto) {
        return ofNullable(dto.shortDateProducts())
            .filter(shortDateDTOList -> !shortDateDTOList.isEmpty())
            .orElseGet(Collections::emptyList)
            .stream()
            .map(shortDateDTO -> new ShortDateProduct(shortDateDTO.unitNumber(), shortDateDTO.productCode() , shortDateDTO.storageLocation()))
            .toList();
    }
}
