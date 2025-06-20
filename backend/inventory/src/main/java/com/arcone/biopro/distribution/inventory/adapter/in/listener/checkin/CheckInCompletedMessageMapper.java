package com.arcone.biopro.distribution.inventory.adapter.in.listener.checkin;

import com.arcone.biopro.distribution.inventory.adapter.in.listener.MessageMapper;
import com.arcone.biopro.distribution.inventory.application.dto.CheckInCompletedInput;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CheckInCompletedMessageMapper extends MessageMapper<CheckInCompletedInput, CheckInCompleted> {

    @Mapping(target = "collectionDate", source = "drawTime")
    @Mapping(target = "collectionLocation", source = "collectionLocation")
    @Mapping(target = "collectionTimeZone", source = "collectionTimeZone")
    @Mapping(target = "checkInLocation", source = "checkInLocation")
    CheckInCompletedInput toInput(CheckInCompleted message);
}
