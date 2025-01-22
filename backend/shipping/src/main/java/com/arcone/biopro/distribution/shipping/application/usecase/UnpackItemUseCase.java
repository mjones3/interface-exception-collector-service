package com.arcone.biopro.distribution.shipping.application.usecase;

import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.ShipmentItemResponseDTO;
import com.arcone.biopro.distribution.shipping.application.dto.NotificationDTO;
import com.arcone.biopro.distribution.shipping.application.dto.NotificationType;
import com.arcone.biopro.distribution.shipping.application.dto.RuleResponseDTO;
import com.arcone.biopro.distribution.shipping.application.dto.UnpackItemsRequest;
import com.arcone.biopro.distribution.shipping.application.mapper.ShipmentMapper;
import com.arcone.biopro.distribution.shipping.application.util.ShipmentServiceMessages;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.ShipmentStatus;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentItemPackedRepository;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentItemRepository;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentItemShortDateProductRepository;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentRepository;
import com.arcone.biopro.distribution.shipping.domain.service.SecondVerificationService;
import com.arcone.biopro.distribution.shipping.domain.service.UnpackItemService;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UnpackItemUseCase implements UnpackItemService {

    private final ShipmentItemPackedRepository shipmentItemPackedRepository;
    private final ShipmentItemRepository shipmentItemRepository;
    private final ShipmentItemShortDateProductRepository shipmentItemShortDateProductRepository;
    private final ShipmentMapper shipmentMapper;
    private final ShipmentRepository shipmentRepository;
    private final SecondVerificationService secondVerificationService;


    @Override
    @Transactional
    @WithSpan("unpackItems")
    public Mono<RuleResponseDTO> unpackItems(UnpackItemsRequest unpackItemsRequest) {

        return shipmentItemRepository.findById(unpackItemsRequest.shipmentItemId())
            .switchIfEmpty(Mono.error(new RuntimeException(ShipmentServiceMessages.UNPACK_SHIPMENT_ITEM_NOT_FOUND_ERROR)))
            .flatMap(shipmentItem -> shipmentRepository.findById(shipmentItem.getShipmentId()))
            .switchIfEmpty(Mono.error(new RuntimeException(ShipmentServiceMessages.UNPACK_SHIPMENT_NOT_FOUND_ERROR)))
            .flatMap(shipment -> {
                if (ShipmentStatus.COMPLETED.equals(shipment.getStatus())) {
                    return Mono.error(new RuntimeException(ShipmentServiceMessages.UNPACK_SHIPMENT_COMPLETED_ERROR));
                }
                return Flux.from(shipmentItemPackedRepository.listAllByShipmentId(shipment.getId()))
                    .switchIfEmpty(Mono.empty())
                    .flatMap(secondVerificationService::markAsVerificationPending)
                    .collectList();
            }).flatMap(resetItems -> {
                return Flux.fromStream(unpackItemsRequest.unpackItems().stream())
                    .flatMap(unpackItemRequest ->
                        Flux.fromStream(resetItems.stream().filter(shipmentItemPacked -> shipmentItemPacked.getUnitNumber().equals(unpackItemRequest.unitNumber()) && shipmentItemPacked.getProductCode().equals(unpackItemRequest.productCode()))
                        )
                    )
                    .switchIfEmpty(Mono.error(new RuntimeException(ShipmentServiceMessages.UNPACK_PRODUCT_NOT_FOUND_ERROR)))
                    .flatMap(shipmentItemPackedRepository::delete)
                    .collectList();

            }).flatMap(removedItems -> Mono.from(getShipmentItemById(unpackItemsRequest.shipmentItemId())).flatMap(shipmentItemResponseDTO -> Mono.just(RuleResponseDTO.builder()
                .ruleCode(HttpStatus.OK)
                .notifications(List.of(NotificationDTO.builder()
                    .message(ShipmentServiceMessages.UNPACK_ITEM_SUCCESS)
                    .statusCode(HttpStatus.OK.value())
                    .notificationType("success")
                    .build()))
                .results(Map.of("results", List.of(shipmentItemResponseDTO)))
                .build())))
            .onErrorResume(error -> {
                log.error("Failed on unpack item {} , {} ", unpackItemsRequest, error.getMessage());
                return buildUnpackItemErrorResponse(error);
            });
    }

    private Mono<ShipmentItemResponseDTO> getShipmentItemById(Long shipmentItemId) {

        return shipmentItemRepository.findById(shipmentItemId)
            .switchIfEmpty(Mono.error(new RuntimeException(ShipmentServiceMessages.SHIPMENT_ITEM_NOT_FOUND_ERROR)))
            .flatMap(shipmentItem -> {
                var shipmentItemResponse = ShipmentItemResponseDTO.builder()
                    .id(shipmentItem.getId())
                    .productFamily(shipmentItem.getProductFamily())
                    .quantity(shipmentItem.getQuantity())
                    .shipmentId(shipmentItem.getShipmentId())
                    .comments(shipmentItem.getComments())
                    .bloodType(shipmentItem.getBloodType())
                    .shortDateProducts(new ArrayList<>())
                    .packedItems(new ArrayList<>())
                    .comments(shipmentItem.getComments())
                    .build();

                log.debug("Fetching Shipment Items Short Date for Shipment Item ID {}", shipmentItem.getId());

                return Flux.from(shipmentItemShortDateProductRepository.findAllByShipmentItemId(shipmentItem.getId()).switchIfEmpty(Flux.empty())).flatMap(shortDateProduct -> {
                    shipmentItemResponse.shortDateProducts().add(shipmentMapper.toShipmentItemShortDateProductResponseDTO(shortDateProduct));
                    return Mono.just(shortDateProduct);
                }).then(Mono.just(shipmentItemResponse));
            }).zipWith(shipmentItemPackedRepository.findAllByShipmentItemId(shipmentItemId).switchIfEmpty(Flux.empty()).collectList())
            .flatMap(tuple2 -> {
                tuple2.getT2().forEach(shipmentItemPacked -> tuple2.getT1().packedItems().add(shipmentMapper.toShipmentItemPackedDTO(shipmentItemPacked)));
                return Mono.just(tuple2.getT1());
            });
    }

    private Mono<RuleResponseDTO> buildUnpackItemErrorResponse(Throwable error) {

        return Mono.just(RuleResponseDTO.builder()
            .ruleCode(HttpStatus.BAD_REQUEST)
            .notifications(List.of(NotificationDTO
                .builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .name(HttpStatus.BAD_REQUEST.name())
                .message(error.getMessage())
                .notificationType(NotificationType.WARN.name())
                .build()))
            .build());

    }
}
