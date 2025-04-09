package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.UseCaseNotificationDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseNotificationOutput;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UseCaseNotificationDtoMapper {

    @Mapping(source = "useCaseMessage.type" , target = "type")
    @Mapping(source = "useCaseMessage.code" , target = "code")
    @Mapping(source = "useCaseMessage.message" , target = "message")
    UseCaseNotificationDTO toDto(UseCaseNotificationOutput useCaseNotificationOutput);

    List<UseCaseNotificationDTO> toDto(List<UseCaseNotificationOutput> useCaseNotificationOutput);
}
