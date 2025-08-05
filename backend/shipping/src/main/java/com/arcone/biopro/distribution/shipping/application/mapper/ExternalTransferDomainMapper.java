package com.arcone.biopro.distribution.shipping.application.mapper;

import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.AddProductTransferRequestDTO;
import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.CompleteExternalTransferRequestDTO;
import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.CreateExternalTransferRequestDTO;
import com.arcone.biopro.distribution.shipping.application.dto.AddProductTransferCommandDTO;
import com.arcone.biopro.distribution.shipping.application.dto.CompleteExternalTransferCommandDTO;
import com.arcone.biopro.distribution.shipping.application.dto.CustomerDTO;
import com.arcone.biopro.distribution.shipping.application.dto.ExternalTransferDTO;
import com.arcone.biopro.distribution.shipping.application.dto.ExternalTransferItemDTO;
import com.arcone.biopro.distribution.shipping.domain.model.CreateExternalTransferCommand;
import com.arcone.biopro.distribution.shipping.domain.model.ExternalTransfer;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.ExternalTransferStatus;
import com.arcone.biopro.distribution.shipping.domain.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class ExternalTransferDomainMapper {

    private final CustomerService customerService;

    public Mono<ExternalTransfer> toDomain(CreateExternalTransferCommand externalTransferCommand){
        try{
            return Mono.just(new ExternalTransfer(null, externalTransferCommand.customerCode()
                , null, externalTransferCommand.hospitalTransferId()
                , externalTransferCommand.transferDate() , externalTransferCommand.createEmployeeId()
                , ExternalTransferStatus.PENDING , null, customerService));
        }catch (Exception e){
            log.error("Not able to map to domain external transfer {}",e.getMessage());
            return Mono.error(e);
        }
    }

    public ExternalTransferDTO toDTO(ExternalTransfer externalTransfer){
        return ExternalTransferDTO
            .builder()
            .id(externalTransfer.getId())
            .transferDate(externalTransfer.getTransferDate())
            .customerTo(CustomerDTO
                .builder()
                .code(externalTransfer.getCustomerTo().getCode())
                .name(externalTransfer.getCustomerTo().getName())
                .build())
            .customerFrom(Optional.ofNullable(externalTransfer.getCustomerFrom()).map(customer -> CustomerDTO
                .builder()
                .code(customer.getCode())
                .name(customer.getName())
                .build()).orElse(null))
            .hospitalTransferId(externalTransfer.getHospitalTransferId())
            .createEmployeeId(externalTransfer.getCreateEmployeeId())
            .status(externalTransfer.getStatus().name())
            .externalTransferItems(Optional.ofNullable(externalTransfer.getExternalTransferItems())
                .filter(list -> !list.isEmpty())
                .orElseGet(Collections::emptyList)
                .stream()
                .map(item -> ExternalTransferItemDTO
                    .builder()
                    .id(item.getId())
                    .externalTransferId(item.getExternalTransferId())
                    .productCode(item.getProduct().getProductCode())
                    .unitNumber(item.getProduct().getUnitNumber())
                    .productFamily(item.getProduct().getProductFamily())
                    .createdByEmployeeId(item.getCreatedByEmployeeId())
                    .build())
                .toList())
            .build();
    }

    public CreateExternalTransferCommand toCommand(CreateExternalTransferRequestDTO createExternalTransferRequestDTO) {

        return CreateExternalTransferCommand
            .builder()
            .customerCode(createExternalTransferRequestDTO.customerCode())
            .hospitalTransferId(createExternalTransferRequestDTO.hospitalTransferId())
            .createEmployeeId(createExternalTransferRequestDTO.createEmployeeId())
            .transferDate(createExternalTransferRequestDTO.transferDate())
            .build();

    }

    public AddProductTransferCommandDTO toCommand(AddProductTransferRequestDTO addProductTransferRequestDTO) {
        return AddProductTransferCommandDTO.builder()
            .externalTransferId(addProductTransferRequestDTO.externalTransferId())
            .unitNumber(addProductTransferRequestDTO.unitNumber())
            .productCode(addProductTransferRequestDTO.productCode())
            .employeeId(addProductTransferRequestDTO.employeeId())
            .build();
    }

    public CompleteExternalTransferCommandDTO toCommand(CompleteExternalTransferRequestDTO completeExternalTransferRequestDTO) {
        return CompleteExternalTransferCommandDTO.builder()
            .externalTransferId(completeExternalTransferRequestDTO.externalTransferId())
            .hospitalTransferId(completeExternalTransferRequestDTO.hospitalTransferId())
            .employeeId(completeExternalTransferRequestDTO.employeeId())
            .build();
    }
}
