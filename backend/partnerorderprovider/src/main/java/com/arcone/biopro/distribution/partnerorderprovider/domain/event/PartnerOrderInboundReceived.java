package com.arcone.biopro.distribution.partnerorderprovider.domain.event;

import com.arcone.biopro.distribution.partnerorderprovider.domain.model.PartnerOrder;

import java.io.Serializable;

public record PartnerOrderInboundReceived(
    PartnerOrder partnerOrder
) implements Serializable {
}
