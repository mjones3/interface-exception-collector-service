package com.arcone.biopro.distribution.inventory.adapter.in.listener.labelinvalidated;

import com.arcone.biopro.distribution.inventory.adapter.in.listener.MessageMapper;
import com.arcone.biopro.distribution.inventory.application.dto.LabelInvalidatedInput;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LabelInvalidatedMessageMapper extends MessageMapper<LabelInvalidatedInput, LabelInvalidatedMessage> {

    LabelInvalidatedInput toInput(LabelInvalidatedMessage message);

}
