package com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.ShipmentHistoryOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.ShipmentHistory;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring" , unmappedTargetPolicy = ReportingPolicy.IGNORE )
public interface ShipmentHistoryOutputMapper {

    ShipmentHistoryOutput toOutput(ShipmentHistory shipmentHistory);

}
