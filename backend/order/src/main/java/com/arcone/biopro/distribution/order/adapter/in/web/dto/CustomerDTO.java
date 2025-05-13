package com.arcone.biopro.distribution.order.adapter.in.web.dto;

import com.arcone.biopro.distribution.order.infrastructure.service.dto.CustomerAddressDTO;
import lombok.Builder;

import java.util.List;

@Builder
public record CustomerDTO(
    String code,
    String externalId,
    String name,
    String departmentCode,
    String departmentName,
    String phoneNumber,
    List<CustomerAddressDTO> addresses,
    String active
) {}
