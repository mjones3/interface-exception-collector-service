package com.arcone.biopro.distribution.partnerorderprovider.adapter.in.web.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@NoArgsConstructor
public class OrderPickTypeDTO implements Serializable {

    private Boolean WillCallPickUp;

    private String phoneNumber;
}
