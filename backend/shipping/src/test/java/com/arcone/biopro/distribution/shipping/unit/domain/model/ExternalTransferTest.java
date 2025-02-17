package com.arcone.biopro.distribution.shipping.unit.domain.model;

import com.arcone.biopro.distribution.shipping.domain.model.ExternalTransfer;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.ExternalTransferStatus;
import com.arcone.biopro.distribution.shipping.domain.service.CustomerService;
import com.arcone.biopro.distribution.shipping.infrastructure.service.dto.CustomerDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

class ExternalTransferTest {

    @Test
    void shouldCreate() {

        var customerService = Mockito.mock(CustomerService.class);
        Mockito.when(customerService.getCustomerByCode(Mockito.anyString())).thenReturn(Mono.just(CustomerDTO
            .builder()
                .code("code")
                .name("Name")
            .build()));

        var response = new ExternalTransfer(1L, "123", null, "A123", LocalDate.now(), "employee-id", ExternalTransferStatus.PENDING,customerService);

        Assertions.assertNotNull(response);
    }

    @Test
    void shouldNotCreate() {

        var customerService = Mockito.mock(CustomerService.class);
        Mockito.when(customerService.getCustomerByCode(Mockito.anyString())).thenReturn(Mono.just(CustomerDTO
            .builder()
            .code("code")
            .name("Name")
            .build()));

        Assertions.assertThrows(IllegalArgumentException.class, () -> new ExternalTransfer(1L, null, null, "A123", LocalDate.now(), "employee-id", ExternalTransferStatus.PENDING,customerService), "Customer cannot be null");
        Assertions.assertThrows(IllegalArgumentException.class, () -> new ExternalTransfer(1L, null, "123", "A123", null, "employee-id", ExternalTransferStatus.PENDING,customerService), "Transfer date cannot be null");
        Assertions.assertThrows(IllegalArgumentException.class, () -> new ExternalTransfer(1L, null, "123", "A123", LocalDate.now().plusDays(2), "employee-id", ExternalTransferStatus.PENDING,customerService), "Transfer date cannot be in the future");
        Assertions.assertThrows(IllegalArgumentException.class, () -> new ExternalTransfer(1L, "123", null, "A123", LocalDate.now(), null, ExternalTransferStatus.PENDING,customerService), "Employee ID cannot be null");
        Assertions.assertThrows(IllegalArgumentException.class, () -> new ExternalTransfer(1L, "123", null, "A123", LocalDate.now(), "employee-id", null,customerService),"Status cannot be null");

        Mockito.when(customerService.getCustomerByCode(Mockito.anyString())).thenReturn(Mono.empty());
        Assertions.assertThrows(IllegalArgumentException.class, () -> new ExternalTransfer(1L, "123", null, "A123", LocalDate.now(), "employee-id",  ExternalTransferStatus.PENDING,customerService),"Customer To should be valid");


    }

}
