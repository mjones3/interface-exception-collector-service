package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.ShippingSummaryReportDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.ShippingSummaryReportOutput;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ShippingSummaryReportDtoMapper {

   ShippingSummaryReportDTO toDto(ShippingSummaryReportOutput shippingSummaryReportOutput);

}
