package com.arcone.biopro.distribution.shipping.application.usecase;

import com.arcone.biopro.distribution.shipping.application.dto.CancelSecondVerificationRequest;
import com.arcone.biopro.distribution.shipping.application.dto.NotificationDTO;
import com.arcone.biopro.distribution.shipping.application.dto.NotificationType;
import com.arcone.biopro.distribution.shipping.application.dto.RuleResponseDTO;
import com.arcone.biopro.distribution.shipping.application.exception.ShipmentValidationException;
import com.arcone.biopro.distribution.shipping.application.util.ShipmentServiceMessages;
import com.arcone.biopro.distribution.shipping.domain.model.Shipment;
import com.arcone.biopro.distribution.shipping.domain.model.ShipmentItemPacked;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.SecondVerification;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.ShipmentStatus;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentItemPackedRepository;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentRepository;
import com.arcone.biopro.distribution.shipping.domain.service.CancelSecondVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class CancelSecondVerificationUseCase implements CancelSecondVerificationService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentItemPackedRepository shipmentItemPackedRepository;
    private static final String SHIPMENT_DETAILS_URL = "/shipment/%s/shipment-details";

    @Override
    public Mono<RuleResponseDTO> cancelSecondVerification(CancelSecondVerificationRequest cancelSecondVerificationRequest) {
        return this.validateShipment(cancelSecondVerificationRequest,Boolean.FALSE)
            .flatMap(this::cancelVerification)
            .onErrorResume(error -> {
                log.error("Failed on cancel second verification {} , {} ", cancelSecondVerificationRequest, error.getMessage());
                return buildErrorResponse(error);
            });
    }

    @Override
    public Mono<RuleResponseDTO> confirmCancelSecondVerification(CancelSecondVerificationRequest cancelSecondVerificationRequest) {
            return this.validateShipment(cancelSecondVerificationRequest,Boolean.TRUE)
            .flatMap(this::cancelVerification)
            .onErrorResume(error -> {
                log.error("Failed on confirm cancel second verification {} , {} ", cancelSecondVerificationRequest, error.getMessage());
                return buildErrorResponse(error);
            });
    }

    private Mono<RuleResponseDTO> cancelVerification(Shipment shipment) {
        return this.resetVerification(shipment).flatMap(result ->
             Mono.just(RuleResponseDTO.builder()
                    .ruleCode(HttpStatus.OK)
                    .notifications(List.of(NotificationDTO.builder()
                        .message(ShipmentServiceMessages.SECOND_VERIFICATION_CANCEL_SUCCESS)
                        .statusCode(HttpStatus.OK.value())
                        .notificationType(NotificationType.SUCCESS.name())
                        .build()))
                    ._links(Map.of("next", String.format(SHIPMENT_DETAILS_URL,shipment.getId())))
                    .build()));
    }

    private Mono<Shipment> validateShipment(CancelSecondVerificationRequest cancelSecondVerificationRequest , boolean confirmed) {
        log.debug("Validating Shipment {}", cancelSecondVerificationRequest.shipmentId());

        var shipment = shipmentRepository.findById(cancelSecondVerificationRequest.shipmentId())
            .switchIfEmpty(Mono.error(new RuntimeException(ShipmentServiceMessages.SHIPMENT_NOT_FOUND_ERROR)));

        var countIneligible = shipmentItemPackedRepository.countIneligibleByShipmentId(cancelSecondVerificationRequest.shipmentId());

        var verifiedList = shipmentItemPackedRepository.listAllVerifiedByShipmentId(cancelSecondVerificationRequest.shipmentId()).collectList();

        return Mono.zip(shipment,countIneligible,verifiedList).flatMap(tuple -> {

            if(ShipmentStatus.COMPLETED.equals(tuple.getT1().getStatus())) {
                return Mono.error(new RuntimeException(ShipmentServiceMessages.SECOND_VERIFICATION_WITH_SHIPMENT_COMPLETED_ERROR));
            }

            if(tuple.getT2() > 0){
                return Mono.error(new ShipmentValidationException(ShipmentServiceMessages.SECOND_VERIFICATION_WITH_INELIGIBLE_PRODUCTS_ERROR
                    ,List.of(NotificationDTO.builder()
                    .name("SECOND_VERIFICATION_WITH_INELIGIBLE_PRODUCTS_ERROR")
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .message(ShipmentServiceMessages.SECOND_VERIFICATION_WITH_INELIGIBLE_PRODUCTS_ERROR)
                    .notificationType(NotificationType.WARN.name())
                    .build()),null, NotificationType.WARN.name())
                );
            }

            if(Boolean.FALSE.equals(confirmed) && !tuple.getT3().isEmpty()){
                return Mono.error(new ShipmentValidationException(ShipmentServiceMessages.SECOND_VERIFICATION_CANCEL_CONFIRMATION
                    ,List.of(NotificationDTO.builder()
                    .name("SECOND_VERIFICATION_CANCEL_CONFIRMATION")
                    .statusCode(HttpStatus.OK.value())
                    .code(HttpStatus.OK.value())
                    .message(ShipmentServiceMessages.SECOND_VERIFICATION_CANCEL_CONFIRMATION)
                    .notificationType(NotificationType.CONFIRMATION.name())
                    .build()),null, NotificationType.CONFIRMATION.name())
                );
            }

            return Mono.just(tuple.getT1());
        });

    }


    private Mono<RuleResponseDTO> buildErrorResponse(Throwable error) {
        if (error instanceof ShipmentValidationException exception) {
            return Mono.just(RuleResponseDTO.builder()
                .ruleCode(HttpStatus.valueOf(exception.getNotifications().getFirst().statusCode()))
                .notifications(exception.getNotifications())
                .build());

        } else {
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

    private Mono<ShipmentItemPacked> markAsVerificationPending(ShipmentItemPacked itemPacked) {
        itemPacked.setSecondVerification(SecondVerification.PENDING);
        itemPacked.setVerificationDate(null);
        itemPacked.setVerifiedByEmployeeId(null);
        itemPacked.setIneligibleStatus(null);
        itemPacked.setIneligibleAction(null);
        itemPacked.setIneligibleReason(null);
        itemPacked.setIneligibleMessage(null);
        return shipmentItemPackedRepository.save(itemPacked);
    }

    private Mono<Shipment> resetVerification(Shipment shipment){
        return Flux.from(shipmentItemPackedRepository.listAllByShipmentId(shipment.getId()))
            .flatMap(this::markAsVerificationPending)
            .then(Mono.just(shipment));
    }
}
