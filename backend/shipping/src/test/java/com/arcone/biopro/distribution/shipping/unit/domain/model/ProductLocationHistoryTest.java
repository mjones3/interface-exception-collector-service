package com.arcone.biopro.distribution.shipping.unit.domain.model;

import com.arcone.biopro.distribution.shipping.domain.model.ProductLocationHistory;
import com.arcone.biopro.distribution.shipping.domain.service.CustomerService;
import com.arcone.biopro.distribution.shipping.infrastructure.service.dto.CustomerDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;

class ProductLocationHistoryTest {

    @Test
    void shouldCreate() {

        var customerService = Mockito.mock(CustomerService.class);
        Mockito.when(customerService.getCustomerByCode(Mockito.anyString())).thenReturn(Mono.just(CustomerDTO
            .builder()
            .code("code")
            .name("Name")
            .build()));

        var history = new ProductLocationHistory(1L, "A123", "customerNameTo", "customerCodeFrom", "customerNameFrom", "EXTERNAL_TRANSFER",
            "unitNumber","productCode","createdByEmployeeId", ZonedDateTime.now(),customerService);
        Assertions.assertEquals(1L, history.getId());

    }

    @Test
    void shouldNotCreate() {

        var customerService = Mockito.mock(CustomerService.class);
        Assertions.assertThrows(IllegalArgumentException.class, () -> new ProductLocationHistory(1L, null, "customerNameTo", null, "customerNameFrom", "EXTERNAL_TRANSFER",
            "unitNumber","productCode","createdByEmployeeId",ZonedDateTime.now(),customerService), "Customer To cannot be null");


        Mockito.when(customerService.getCustomerByCode(Mockito.anyString())).thenReturn(Mono.just(CustomerDTO
            .builder()
            .code("code")
            .name("Name")
            .build()));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new ProductLocationHistory(1L, "123", "customerNameTo", null, null, "EXTERNAL_TRANSFER",
            null,"productCode","createdByEmployeeId",ZonedDateTime.now(),customerService), "Product cannot be null");


        Assertions.assertThrows(IllegalArgumentException.class, () -> new ProductLocationHistory(1L, "123", "customerNameTo", null, null, "EXTERNAL_TRANSFER",
            "UNIT","productCode",null,ZonedDateTime.now(),customerService), "Employee ID cannot be null");


        Mockito.when(customerService.getCustomerByCode(Mockito.eq("CUSTOMER-FROM"))).thenReturn(Mono.empty());
        Assertions.assertThrows(IllegalArgumentException.class, () -> new ProductLocationHistory(1L, "123", "customerNameTo", "CUSTOMER-FROM", "customerNameFrom", "EXTERNAL_TRANSFER",
            "unitNumber","productCode","createdByEmployeeId",ZonedDateTime.now(),customerService), "Customer From should be valid");


    }

}
