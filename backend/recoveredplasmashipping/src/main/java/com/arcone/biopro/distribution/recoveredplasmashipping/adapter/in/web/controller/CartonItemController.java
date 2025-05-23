package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.controller;

import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.CartonDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.PackCartonItemRequestDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.RemoveCartonItemRequestDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.UseCaseResponseDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.mapper.CommandRequestDTOMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.mapper.UseCaseResponseMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.PackCartonItemService;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.RemoveCartonItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@Slf4j
@RequiredArgsConstructor
public class CartonItemController {

    private final PackCartonItemService packCartonItemService;
    private final CommandRequestDTOMapper commandRequestDTOMapper;
    private final UseCaseResponseMapper useCaseResponseMapper;
    private final RemoveCartonItemService removeCartonItemService;

    @MutationMapping("packCartonItem")
    public Mono<UseCaseResponseDTO<CartonDTO>> packCartonItem(@Argument("packCartonItemRequest") PackCartonItemRequestDTO packCartonItemRequestDTO) {
        log.debug("Request to pack a Carton Item : {}", packCartonItemRequestDTO);
        return packCartonItemService.packItem(commandRequestDTOMapper.toInputCommand(packCartonItemRequestDTO))
            .map(useCaseResponseMapper::toUseCasePackCartonItemDTO);
    }

    @MutationMapping("removeCartonItems")
    public Mono<UseCaseResponseDTO<CartonDTO>> removeCartonItems(@Argument("removeCartonItemRequest") RemoveCartonItemRequestDTO removeCartonItemRequestDTO) {
        log.debug("Request to remove Carton Items : {}", removeCartonItemRequestDTO);
        return removeCartonItemService.removeCartonItem(commandRequestDTOMapper.toInputCommand(removeCartonItemRequestDTO))
            .map(useCaseResponseMapper::toUseCasePackCartonItemDTO);
    }

}
