package com.arcone.biopro.distribution.receiving.adapter.in.web.mapper;

import com.arcone.biopro.distribution.receiving.adapter.in.web.dto.UseCaseNotificationDTO;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseNotificationOutput;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UseCaseNotificationDtoMapper {

    @Mapping(source = "useCaseMessage.type" , target = "type")
    @Mapping(source = "useCaseMessage.code" , target = "code")
    @Mapping(source = "useCaseMessage.message" , target = "message")
    @Mapping(source = "useCaseMessage.action" , target = "action")
    @Mapping(source = "useCaseMessage.reason" , target = "reason")
    @Mapping(source = "useCaseMessage.details" , target = "details")
    @Mapping(source = "useCaseMessage.name" , target = "name")
    UseCaseNotificationDTO toDto(UseCaseNotificationOutput useCaseNotificationOutput);

    List<UseCaseNotificationDTO> toDto(List<UseCaseNotificationOutput> useCaseNotificationOutput);
}
