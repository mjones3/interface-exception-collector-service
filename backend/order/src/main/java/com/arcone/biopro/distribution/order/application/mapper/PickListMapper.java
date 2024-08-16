package com.arcone.biopro.distribution.order.application.mapper;

import com.arcone.biopro.distribution.order.adapter.in.web.dto.PickListDTO;
import com.arcone.biopro.distribution.order.domain.model.PickList;
import org.springframework.stereotype.Component;

@Component
public class PickListMapper {

    public PickListDTO mapToDTO(PickList pickList) {
        return PickListDTO.builder().build();
    }
}
