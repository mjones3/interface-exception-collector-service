package com.arcone.biopro.distribution.irradiation.adapter.in.listener.unsuitable;

import com.arcone.biopro.distribution.irradiation.adapter.in.listener.MessageMapper;
import com.arcone.biopro.distribution.irradiation.application.dto.UnsuitableInput;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UnsuitableMessageMapper extends MessageMapper<UnsuitableInput, UnitUnsuitable> {

    UnsuitableInput toInput(UnitUnsuitable message);
}
