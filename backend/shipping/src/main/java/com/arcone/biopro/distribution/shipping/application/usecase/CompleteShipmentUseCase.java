package com.arcone.biopro.distribution.shipping.application.usecase;

import com.arcone.biopro.distribution.shipping.application.dto.CompleteShipmentRequest;
import com.arcone.biopro.distribution.shipping.application.dto.NotificationDTO;
import com.arcone.biopro.distribution.shipping.application.dto.NotificationType;
import com.arcone.biopro.distribution.shipping.application.dto.RuleResponseDTO;
import com.arcone.biopro.distribution.shipping.application.exception.ShipmentValidationException;
import com.arcone.biopro.distribution.shipping.application.mapper.ShipmentEventMapper;
import com.arcone.biopro.distribution.shipping.application.util.ShipmentServiceMessages;
import com.arcone.biopro.distribution.shipping.domain.model.Shipment;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.ShipmentStatus;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentItemPackedRepository;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentRepository;
import com.arcone.biopro.distribution.shipping.domain.service.CompleteShipmentService;
import com.arcone.biopro.distribution.shipping.domain.service.ConfigService;
import com.arcone.biopro.distribution.shipping.domain.service.ShipmentService;
import com.arcone.biopro.distribution.shipping.infrastructure.controller.dto.InventoryValidationRequest;
import com.arcone.biopro.distribution.shipping.infrastructure.service.FacilityServiceMock;
import com.arcone.biopro.distribution.shipping.infrastructure.service.InventoryRsocketClient;
import com.arcone.biopro.distribution.shipping.infrastructure.service.dto.FacilityDTO;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Boolean.TRUE;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompleteShipmentUseCase implements CompleteShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ShipmentEventMapper shipmentEventMapper;
    private final FacilityServiceMock facilityServiceMock;
    private final ConfigService configService;
    private final ShipmentService shipmentService;
    private final ShipmentItemPackedRepository shipmentItemPackedRepository;
    private static final String SHIPMENT_DETAILS_URL = "/shipment/%s/shipment-details";
    private static final String SHIPMENT_VERIFICATION_URL = "/shipment/%s/verify-products";
    private final InventoryRsocketClient inventoryRsocketClient;


    @Override
    @WithSpan("completeShipment")
    @Transactional
    public Mono<RuleResponseDTO> completeShipment(CompleteShipmentRequest request) {

        return shipmentRepository.findById(request.shipmentId())
            .switchIfEmpty(Mono.error(new RuntimeException(ShipmentServiceMessages.SHIPMENT_NOT_FOUND_ERROR)))
            .flatMap(this::validateShipment)
            .flatMap(shipment -> this.updateShipment(shipment,request))
            .flatMap(this::raiseShipmentCompleteEvent)
            .flatMap(shipment -> Mono.just(RuleResponseDTO.builder()
                .ruleCode(HttpStatus.OK)
                .notifications(List.of(NotificationDTO.builder()
                    .message(ShipmentServiceMessages.SHIPMENT_COMPLETED_SUCCESS)
                    .statusCode(HttpStatus.OK.value())
                    .notificationType("success")
                    .build()))
                .results(Map.of("results", List.of(shipment)))
                ._links(Map.of("next", String.format(SHIPMENT_DETAILS_URL,shipment.getId())))
                .build()))
            .onErrorResume(error -> {
                log.error("Failed on complete shipment {} , {} ", request, error.getMessage());
                return buildErrorResponse(error);
            });
    }

    private Mono<Shipment> raiseShipmentCompleteEvent(Shipment shipment){

        return facilityServiceMock.getFacilityId(shipment.getLocationCode())
            .map(FacilityDTO::name)
            .zipWith(shipmentService.getShipmentById(shipment.getId()))
            .flatMap(tuple -> {
                applicationEventPublisher.publishEvent(shipmentEventMapper.toShipmentCompletedEvent(tuple.getT2(),tuple.getT1()));
                return Mono.just(shipment);
            }).then(Mono.just(shipment));

    }

    private Mono<Shipment> updateShipment(Shipment shipment , CompleteShipmentRequest request){
        if(ShipmentStatus.COMPLETED.equals(shipment.getStatus())){
            return Mono.error(new RuntimeException(ShipmentServiceMessages.SHIPMENT_COMPLETED_ERROR));
        }
        shipment.setCompleteDate(ZonedDateTime.now(ZoneId.of("UTC")));
        shipment.setCompletedByEmployeeId(request.employeeId());
        shipment.setStatus(ShipmentStatus.COMPLETED);
        return shipmentRepository.save(shipment);

    }

    private Mono<RuleResponseDTO> buildErrorResponse(Throwable error) {
        if(error instanceof ShipmentValidationException exception){
           return Mono.just(RuleResponseDTO.builder()
                .ruleCode(HttpStatus.BAD_REQUEST)
                   .results(((ShipmentValidationException) error).getResults())
                .notifications(List.of(NotificationDTO.builder()
                    .message(exception.getMessage())
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .notificationType(NotificationType.WARN.name())
                    .build()))
                ._links(Map.of("next",exception.getNextUrl()))
                .build());

        }else{
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

    private Mono<Shipment> validateShipment(Shipment shipment){
        log.debug("Validating Shipment {}",shipment.getId());

        var secondVerificationActive = configService.findShippingSecondVerificationActive();
        var countVerification = shipmentItemPackedRepository.countVerificationPendingByShipmentId(shipment.getId());

        return Mono.zip(secondVerificationActive,countVerification).flatMap(tuple -> {
            if(TRUE.equals(tuple.getT1())){
                if(tuple.getT2() > 0){
                    return Mono.error(new ShipmentValidationException(ShipmentServiceMessages.SECOND_VERIFICATION_NOT_COMPLETED_ERROR
                        ,List.of(NotificationDTO.builder()
                        .name("SECOND_VERIFICATION_NOT_COMPLETED_ERROR")
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .message(ShipmentServiceMessages.SECOND_VERIFICATION_NOT_COMPLETED_ERROR)
                        .notificationType(NotificationType.WARN.name())
                        .build()),String.format(SHIPMENT_VERIFICATION_URL,shipment.getId()) )
                    );
                }

                return shipmentItemPackedRepository.listAllVerifiedByShipmentId(shipment.getId()).flatMap(itemPacked -> {
                        return inventoryRsocketClient.validateInventory(InventoryValidationRequest.builder()
                                .productCode(itemPacked.getProductCode())
                                .locationCode(shipment.getLocationCode())
                                .unitNumber(itemPacked.getUnitNumber()).build())
                            .flatMap(inventoryValidationResponseDTO -> {
                                if(inventoryValidationResponseDTO!= null && inventoryValidationResponseDTO.inventoryNotificationsDTO() != null && !inventoryValidationResponseDTO.inventoryNotificationsDTO().isEmpty() ){
                                    return Mono.just(inventoryValidationResponseDTO);
                                }else{
                                    return  Mono.empty();
                                }
                            })
                            .onErrorResume(error -> {
                                return Mono.error(new ShipmentValidationException(ShipmentServiceMessages.INVENTORY_SERVICE_NOT_AVAILABLE_ERROR
                                    ,List.of(NotificationDTO.builder()
                                    .name("INVENTORY_SERVICE_IS_DOWN")
                                    .statusCode(HttpStatus.BAD_REQUEST.value())
                                    .message(ShipmentServiceMessages.INVENTORY_SERVICE_NOT_AVAILABLE_ERROR)
                                    .notificationType(NotificationType.SYSTEM.name())
                                    .build()),String.format(SHIPMENT_VERIFICATION_URL,shipment.getId()) )
                                );
                            });

                    }).collectList()
                    .flatMap(validationList -> {
                        if(!validationList.isEmpty()){
                            Map<String, List<?>> results  = new HashMap<>();
                            results.put("validations", validationList);
                            return Mono.error(new ShipmentValidationException(ShipmentServiceMessages.SHIPMENT_VALIDATION_COMPLETED_ERROR
                                ,List.of(NotificationDTO.builder()
                                .name("SHIPMENT_VALIDATION_COMPLETED_ERROR")
                                .statusCode(HttpStatus.BAD_REQUEST.value())
                                .message(ShipmentServiceMessages.SHIPMENT_VALIDATION_COMPLETED_ERROR)
                                .notificationType(NotificationType.WARN.name())
                                .build()),String.format(SHIPMENT_VERIFICATION_URL,shipment.getId()) , results )
                            );
                        }else{
                            return Mono.just(shipment);
                        }
                    });


           }else {
                return Mono.just(shipment);
            }
        });
    }
}

