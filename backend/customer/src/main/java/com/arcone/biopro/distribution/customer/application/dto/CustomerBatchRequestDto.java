package com.arcone.biopro.distribution.customer.application.dto;

import lombok.Data;
import java.util.List;

@Data
public class CustomerBatchRequestDto {
    private String batchId;
    private List<CustomerDto> customers;
}
