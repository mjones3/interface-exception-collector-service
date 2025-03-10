package com.arcone.biopro.distribution.inventory.adapter.in.listener.unsuitable;

import com.arcone.biopro.distribution.inventory.adapter.in.listener.MessageMapper;
import com.arcone.biopro.distribution.inventory.application.dto.UnsuitableInput;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UnsuitableMessageMapper extends MessageMapper<UnsuitableInput, UnsuitableMessage> {

    UnsuitableInput toInput(UnsuitableMessage message);
}
