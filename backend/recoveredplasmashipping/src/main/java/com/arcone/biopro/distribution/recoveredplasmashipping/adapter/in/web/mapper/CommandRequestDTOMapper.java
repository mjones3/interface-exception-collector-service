package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.QuerySortDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.RecoveredPlasmaShipmentQueryCommandRequestDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.QueryOrderByOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.QuerySortOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.RecoveredPlasmaShipmentQueryCommandInput;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CommandRequestDTOMapper {

    RecoveredPlasmaShipmentQueryCommandInput toInputCommand(RecoveredPlasmaShipmentQueryCommandRequestDTO queryCommandRequestDTO);
    default QuerySortOutput toQuerySortOutput(QuerySortDTO querySortDTO) {
        return querySortDTO != null ? QuerySortOutput.builder()
            .queryOrderByList(querySortDTO.orderByList().stream().map(sortDto -> new QueryOrderByOutput(sortDto.property(),sortDto.direction())).toList())
            .build() : null;
    }
}
