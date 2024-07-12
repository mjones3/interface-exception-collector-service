package com.arcone.biopro.distribution.partnerorderproviderservice.application.usecase;

import com.arcone.biopro.distribution.partnerorderproviderservice.adapter.in.web.dto.OrderInboundDTO;
import com.arcone.biopro.distribution.partnerorderproviderservice.application.dto.ValidationResponseDTO;
import com.arcone.biopro.distribution.partnerorderproviderservice.domain.event.PartnerOrderInboundReceived;
import com.arcone.biopro.distribution.partnerorderproviderservice.domain.model.PartnerOrder;
import com.arcone.biopro.distribution.partnerorderproviderservice.domain.model.PartnerOrderItem;
import com.arcone.biopro.distribution.partnerorderproviderservice.domain.model.PartnerOrderPickUpType;
import com.arcone.biopro.distribution.partnerorderproviderservice.domain.service.OrderInboundService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderInboundUseCase implements OrderInboundService {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public ValidationResponseDTO receiveOrderInbound(OrderInboundDTO orderInboundDTO) {
        UUID uuid = UUID.randomUUID();
        var partnerOrder = new PartnerOrder(
            uuid,
            orderInboundDTO.getExternalId(),
            orderInboundDTO.getOrderStatus()
            , orderInboundDTO.getLocationCode()
            , orderInboundDTO.getCreateDate()
            , orderInboundDTO.getCreateEmployeeCode()
            , orderInboundDTO.getShipmentType()
            , orderInboundDTO.getDeliveryType()
            , orderInboundDTO.getShippingMethod()
            , orderInboundDTO.getProductCategory()
            , orderInboundDTO.getDesiredShippingDate()
            , orderInboundDTO.getShippingCustomerCode()
            ,orderInboundDTO.getBillingCustomerCode()
            , orderInboundDTO.getComments()
            , Objects.nonNull(orderInboundDTO.getOrderPickType()) ? new PartnerOrderPickUpType(orderInboundDTO.getOrderPickType().getWillCallPickUp(),orderInboundDTO.getOrderPickType().getPhoneNumber()): null);
        if(orderInboundDTO.getOrderItems() != null && !orderInboundDTO.getOrderItems().isEmpty()){
            orderInboundDTO.getOrderItems().forEach(orderItemDTO -> {
                partnerOrder.addItem(new PartnerOrderItem(orderItemDTO.getProductFamily(), orderItemDTO.getBloodType() , orderItemDTO.getQuantity(), orderItemDTO.getComments()));
            });
        }

        applicationEventPublisher.publishEvent(new PartnerOrderInboundReceived(partnerOrder));
        return ValidationResponseDTO
            .builder()
            .id(uuid)
            .status("CREATED")
            .timestamp(ZonedDateTime.now())
            .build();
    }
}
