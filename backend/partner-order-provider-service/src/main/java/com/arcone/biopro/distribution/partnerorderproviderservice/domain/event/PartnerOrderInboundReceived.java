package com.arcone.biopro.distribution.partnerorderproviderservice.domain.event;

import com.arcone.biopro.distribution.partnerorderproviderservice.domain.model.PartnerOrder;

import java.io.Serializable;

public record PartnerOrderInboundReceived(
    PartnerOrder partnerOrder
) implements Serializable {
}
