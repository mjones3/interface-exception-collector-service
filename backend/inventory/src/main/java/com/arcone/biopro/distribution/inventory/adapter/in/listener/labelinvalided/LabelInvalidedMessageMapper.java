package com.arcone.biopro.distribution.inventory.adapter.in.listener.labelinvalided;

import com.arcone.biopro.distribution.inventory.adapter.in.listener.MessageMapper;
import com.arcone.biopro.distribution.inventory.application.dto.LabelInvalidedInput;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LabelInvalidedMessageMapper extends MessageMapper<LabelInvalidedInput, LabelInvalidedMessage> {

    LabelInvalidedInput toInput(LabelInvalidedMessage message);

}
