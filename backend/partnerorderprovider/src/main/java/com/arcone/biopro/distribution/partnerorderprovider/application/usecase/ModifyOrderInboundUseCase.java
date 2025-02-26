package com.arcone.biopro.distribution.partnerorderprovider.application.usecase;

import com.arcone.biopro.distribution.partnerorderprovider.adapter.in.web.dto.ModifyOrderInboundDTO;
import com.arcone.biopro.distribution.partnerorderprovider.application.dto.ValidationResponseDTO;
import com.arcone.biopro.distribution.partnerorderprovider.domain.event.ModifyOrderInboundReceived;
import com.arcone.biopro.distribution.partnerorderprovider.domain.model.ModifyOrder;
import com.arcone.biopro.distribution.partnerorderprovider.domain.model.PartnerOrderItem;
import com.arcone.biopro.distribution.partnerorderprovider.domain.model.PartnerOrderPickUpType;
import com.arcone.biopro.distribution.partnerorderprovider.domain.service.ModifyOrderInboundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ModifyOrderInboundUseCase implements ModifyOrderInboundService {

    private final ApplicationEventPublisher applicationEventPublisher;


    @Override
    public ValidationResponseDTO receiveModifyOrderInbound(ModifyOrderInboundDTO modifyOrderInboundDTO) {
        UUID uuid = UUID.randomUUID();

        var modifyOrder = new ModifyOrder(
            uuid
            ,modifyOrderInboundDTO.getExternalId()
            , modifyOrderInboundDTO.getLocationCode()
            , modifyOrderInboundDTO.getModifyDate()
            , modifyOrderInboundDTO.getModifyEmployeeCode()
            , modifyOrderInboundDTO.getDeliveryType()
            , modifyOrderInboundDTO.getShippingMethod()
            , modifyOrderInboundDTO.getProductCategory()
            , modifyOrderInboundDTO.getDesiredShippingDate()
            , modifyOrderInboundDTO.getComments()
            , Objects.nonNull(modifyOrderInboundDTO.getOrderPickType()) ? new PartnerOrderPickUpType(modifyOrderInboundDTO.getOrderPickType().getWillCallPickUp(),modifyOrderInboundDTO.getOrderPickType().getPhoneNumber()): null);
        if(modifyOrderInboundDTO.getOrderItems() != null && !modifyOrderInboundDTO.getOrderItems().isEmpty()){
            modifyOrderInboundDTO.getOrderItems().forEach(orderItemDTO -> {
                modifyOrder.addItem(new PartnerOrderItem(orderItemDTO.getProductFamily(), orderItemDTO.getBloodType() , orderItemDTO.getQuantity(), orderItemDTO.getComments()));
            });
        }

        applicationEventPublisher.publishEvent(new ModifyOrderInboundReceived(modifyOrder));
        return ValidationResponseDTO
            .builder()
            .id(uuid)
            .status("ACCEPTED")
            .timestamp(ZonedDateTime.now())
            .build();
    }
}
