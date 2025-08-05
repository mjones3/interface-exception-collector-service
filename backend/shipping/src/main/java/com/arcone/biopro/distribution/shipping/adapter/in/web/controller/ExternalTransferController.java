package com.arcone.biopro.distribution.shipping.adapter.in.web.controller;

import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.AddProductTransferRequestDTO;
import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.CancelExternalTransferRequestDTO;
import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.CompleteExternalTransferRequestDTO;
import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.CreateExternalTransferRequestDTO;
import com.arcone.biopro.distribution.shipping.application.dto.CancelExternalTransferRequest;
import com.arcone.biopro.distribution.shipping.application.dto.RuleResponseDTO;
import com.arcone.biopro.distribution.shipping.application.mapper.ExternalTransferDomainMapper;
import com.arcone.biopro.distribution.shipping.application.usecase.CreateExternalTransferUseCase;
import com.arcone.biopro.distribution.shipping.domain.service.AddProductTransferService;
import com.arcone.biopro.distribution.shipping.domain.service.CancelExternalTransferService;
import com.arcone.biopro.distribution.shipping.domain.service.CompleteExternalTransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ExternalTransferController {

    private final CreateExternalTransferUseCase createExternalTransferUseCase;
    private final ExternalTransferDomainMapper externalTransferDomainMapper;
    private final AddProductTransferService addProductTransferService;
    private final CompleteExternalTransferService completeExternalTransferService;
    private final CancelExternalTransferService cancelExternalTransferService;

    @MutationMapping("createExternalTransfer")
    public Mono<RuleResponseDTO> createExternalTransfer(@Argument("createExternalTransferRequest") CreateExternalTransferRequestDTO createExternalTransferRequest) {
        log.debug("Request to create a external transfer {}", createExternalTransferRequest);
        return createExternalTransferUseCase.createExternalTransfer(externalTransferDomainMapper.toCommand(createExternalTransferRequest));
    }

    @MutationMapping("addExternalTransferProduct")
    public Mono<RuleResponseDTO> addExternalTransferProduct(@Argument("addProductTransferRequestDTO") AddProductTransferRequestDTO addProductTransferRequestDTO) {
        log.debug("Request to add a product into a external transfer {}", addProductTransferRequestDTO);
        return addProductTransferService.addProductTransfer(externalTransferDomainMapper.toCommand(addProductTransferRequestDTO));
    }

    @MutationMapping("completeExternalTransfer")
    public Mono<RuleResponseDTO> completeExternalTransfer(@Argument("completeExternalTransferRequestDTO") CompleteExternalTransferRequestDTO completeExternalTransferRequestDTO) {
        log.debug("Request to complete a external transfer {}", completeExternalTransferRequestDTO);
        return completeExternalTransferService.completeExternalTransfer(externalTransferDomainMapper.toCommand(completeExternalTransferRequestDTO));
    }


    @MutationMapping("cancelExternalTransfer")
    public Mono<RuleResponseDTO> cancelExternalTransfer(@Argument("cancelExternalTransferRequestDTO") CancelExternalTransferRequestDTO cancelExternalTransferRequestDTO) {
        log.debug("Request to cancel external Transfer {}", cancelExternalTransferRequestDTO);
        return cancelExternalTransferService.cancelExternalTransfer(new CancelExternalTransferRequest(cancelExternalTransferRequestDTO.externalTransferId(), cancelExternalTransferRequestDTO.employeeId()));
    }

    @MutationMapping("confirmCancelExternalTransfer")
    public Mono<RuleResponseDTO> confirmCancelExternalTransfer(@Argument("cancelExternalTransferRequestDTO") CancelExternalTransferRequestDTO cancelExternalTransferRequestDTO) {
        log.info("Request to confirm cancel external transfer {}", cancelExternalTransferRequestDTO);
        return cancelExternalTransferService.confirmCancelExternalTransfer(new CancelExternalTransferRequest(cancelExternalTransferRequestDTO.externalTransferId(), cancelExternalTransferRequestDTO.employeeId()));
    }

}
