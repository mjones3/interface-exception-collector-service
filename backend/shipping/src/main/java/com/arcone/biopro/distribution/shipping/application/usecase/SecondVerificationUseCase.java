package com.arcone.biopro.distribution.shipping.application.usecase;

import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.VerifyProductResponseDTO;
import com.arcone.biopro.distribution.shipping.application.dto.NotificationDTO;
import com.arcone.biopro.distribution.shipping.application.dto.NotificationType;
import com.arcone.biopro.distribution.shipping.application.dto.RuleResponseDTO;
import com.arcone.biopro.distribution.shipping.application.dto.VerifyItemRequest;
import com.arcone.biopro.distribution.shipping.application.mapper.ShipmentMapper;
import com.arcone.biopro.distribution.shipping.application.util.ShipmentServiceMessages;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.SecondVerification;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentItemPackedRepository;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentRepository;
import com.arcone.biopro.distribution.shipping.domain.service.SecondVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SecondVerificationUseCase implements SecondVerificationService {

    private final ShipmentItemPackedRepository shipmentItemPackedRepository;
    private final ShipmentRepository shipmentRepository;
    private final ShipmentMapper shipmentMapper;

    @Override
    public Mono<RuleResponseDTO> verifyItem(VerifyItemRequest verifyItemRequest) {
        return shipmentItemPackedRepository.findByShipmentIUnitNumberAndProductCode(verifyItemRequest.shipmentId()
                ,verifyItemRequest.unitNumber(), verifyItemRequest.productCode())
            .switchIfEmpty(Mono.error(new RuntimeException(ShipmentServiceMessages.SECOND_VERIFICATION_UNIT_NOT_PACKED_ERROR)))
            .flatMap(shipmentItemPacked -> {
                    shipmentItemPacked.setSecondVerification(SecondVerification.COMPLETED);
                    shipmentItemPacked.setVerificationDate(ZonedDateTime.now());
                    shipmentItemPacked.setVerifiedByEmployeeId(verifyItemRequest.employeeId());
                    return shipmentItemPackedRepository.save(shipmentItemPacked);
                })
                .flatMap(savedPackedItem -> {
                    return Mono.from(getShipmentVerificationDetailsById(verifyItemRequest.shipmentId()))
                        .flatMap(verificationResponse -> Mono.just(RuleResponseDTO.builder()
                        .ruleCode(HttpStatus.OK)
                        .results(Map.of("results", List.of(verificationResponse)))
                        .build()));
                })
            .onErrorResume(error -> {
                log.error("Failed on verify item {} , {} ", verifyItemRequest, error.getMessage());
                return buildVerifyErrorResponse(error);
            });
    }

    @Override
    public Mono<VerifyProductResponseDTO> getVerificationDetailsByShipmentId(Long shipmentId) {
        return shipmentRepository.findById(shipmentId)
            .switchIfEmpty(Mono.error(new RuntimeException(ShipmentServiceMessages.SHIPMENT_NOT_FOUND_ERROR)))
            .flatMap(shipment -> getShipmentVerificationDetailsById(shipment.getId()));
    }

    private Mono<VerifyProductResponseDTO> getShipmentVerificationDetailsById(Long shipmentId) {

        var packedList = shipmentItemPackedRepository.listAllByShipmentId(shipmentId).collectList();
        var verifiedList = shipmentItemPackedRepository.listAllVerifiedByShipmentId(shipmentId).collectList();

        return Mono.zip(packedList,verifiedList).flatMap(tuple -> Mono.just(VerifyProductResponseDTO
            .builder()
                .packedItems(tuple.getT1().stream().map(shipmentMapper::toShipmentItemPackedDTO).toList())
            .verifiedItems(tuple.getT2().stream().map(shipmentMapper::toShipmentItemPackedDTO).toList())
            .build()));
    }

    private Mono<RuleResponseDTO> buildVerifyErrorResponse(Throwable error) {

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
