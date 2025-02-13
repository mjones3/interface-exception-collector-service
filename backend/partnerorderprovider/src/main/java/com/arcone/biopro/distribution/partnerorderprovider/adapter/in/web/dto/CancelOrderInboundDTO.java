package com.arcone.biopro.distribution.partnerorderprovider.adapter.in.web.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class CancelOrderInboundDTO implements Serializable {

    private String externalId;
    private String cancelDate;
    private String cancelEmployeeCode;
    private String cancelReason;

}
