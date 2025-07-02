package com.arcone.biopro.distribution.partnerorderprovider.application.usecase;

import com.arcone.biopro.distribution.partnerorderprovider.adapter.in.web.dto.OrderInboundDTO;
import com.arcone.biopro.distribution.partnerorderprovider.application.dto.ValidationResponseDTO;
import com.arcone.biopro.distribution.partnerorderprovider.domain.event.PartnerOrderInboundReceived;
import com.arcone.biopro.distribution.partnerorderprovider.domain.model.PartnerOrder;
import com.arcone.biopro.distribution.partnerorderprovider.domain.model.PartnerOrderItem;
import com.arcone.biopro.distribution.partnerorderprovider.domain.model.PartnerOrderPickUpType;
import com.arcone.biopro.distribution.partnerorderprovider.domain.service.OrderInboundService;
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
            , Objects.nonNull(orderInboundDTO.getOrderPickType()) ? new PartnerOrderPickUpType(orderInboundDTO.getOrderPickType().getWillCallPickUp(),orderInboundDTO.getOrderPickType().getPhoneNumber()): null
            ,orderInboundDTO.getLabelStatus()
            , orderInboundDTO.getQuarantineProducts());
        if(orderInboundDTO.getOrderItems() != null && !orderInboundDTO.getOrderItems().isEmpty()){
            orderInboundDTO.getOrderItems().forEach(orderItemDTO -> {
                partnerOrder.addItem(new PartnerOrderItem(orderItemDTO.getProductFamily(), orderItemDTO.getBloodType() , orderItemDTO.getQuantity(), orderItemDTO.getComments()));
            });
        }

        applicationEventPublisher.publishEvent(new PartnerOrderInboundReceived(partnerOrder));
        return ValidationResponseDTO
            .builder()
            .id(uuid)
            .status("ACCEPTED")
            .timestamp(ZonedDateTime.now())
            .build();
    }
}
