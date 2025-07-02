package com.arcone.biopro.distribution.irradiation.adapter.in.listener.labelinvalidated;

import com.arcone.biopro.distribution.irradiation.adapter.in.listener.MessageMapper;
import com.arcone.biopro.distribution.irradiation.application.dto.LabelInvalidatedInput;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LabelInvalidatedMessageMapper extends MessageMapper<LabelInvalidatedInput, LabelInvalidated> {

    LabelInvalidatedInput toInput(LabelInvalidated message);

}
