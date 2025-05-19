package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper;


import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.UnacceptableUnitReportItem;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.UnacceptableUnitReportEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UnacceptableUnitReportEntityMapper {

    UnacceptableUnitReportEntity toEntity(UnacceptableUnitReportItem unacceptableUnitReportItem);

    UnacceptableUnitReportItem toModel(UnacceptableUnitReportEntity unacceptableUnitReportEntity);
}



