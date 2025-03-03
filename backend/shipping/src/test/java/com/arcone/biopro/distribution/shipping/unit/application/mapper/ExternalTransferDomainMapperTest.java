package com.arcone.biopro.distribution.shipping.unit.application.mapper;

import com.arcone.biopro.distribution.shipping.application.mapper.ExternalTransferDomainMapper;
import com.arcone.biopro.distribution.shipping.domain.model.CreateExternalTransferCommand;
import com.arcone.biopro.distribution.shipping.domain.model.ExternalTransfer;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.ExternalTransferStatus;
import com.arcone.biopro.distribution.shipping.domain.model.vo.Customer;
import com.arcone.biopro.distribution.shipping.domain.service.CustomerService;
import com.arcone.biopro.distribution.shipping.infrastructure.service.dto.CustomerDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;

class ExternalTransferDomainMapperTest {

    private ExternalTransferDomainMapper externalTransferDomainMapper;
    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerService = Mockito.mock(CustomerService.class);
        Mockito.when(customerService.getCustomerByCode(Mockito.anyString())).thenReturn(Mono.just(CustomerDTO
            .builder()
            .code("code")
            .name("Name")
            .build()));
        externalTransferDomainMapper = new ExternalTransferDomainMapper(customerService);
    }

    @Test
    public void mapToDomain() {

        StepVerifier
            .create(externalTransferDomainMapper.toDomain(CreateExternalTransferCommand
                .builder()
                    .customerCode("123")
                    .hospitalTransferId("123")
                    .transferDate(LocalDate.now())
                    .createEmployeeId("employee-id")
                .build()))
            .consumeNextWith(detail -> {
                Assertions.assertNotNull(detail);
                Assertions.assertEquals("123",detail.getHospitalTransferId());

            })
            .verifyComplete();


    }

    @Test
    public void shouldNotMapToDomain() {

        StepVerifier
            .create(externalTransferDomainMapper.toDomain(CreateExternalTransferCommand.builder().build()))
            .verifyError();
    }

    @Test
    public void shouldMapToDtoWhenCustomerFromIsNull(){
        var mock  = Mockito.mock(ExternalTransfer.class);
        Mockito.when(mock.getStatus()).thenReturn(ExternalTransferStatus.PENDING);
        Mockito.when(mock.getCustomerTo()).thenReturn(Mockito.mock(Customer.class));

        var dto = externalTransferDomainMapper.toDTO(mock);

        Assertions.assertNotNull(dto);
        Assertions.assertEquals("PENDING",dto.status());
        Assertions.assertNull(dto.customerFrom());
    }

    @Test
    public void shouldMapToDtoWhenCustomerFromIsNotNull(){
        var mock  = Mockito.mock(ExternalTransfer.class);
        Mockito.when(mock.getStatus()).thenReturn(ExternalTransferStatus.PENDING);
        Mockito.when(mock.getCustomerTo()).thenReturn(Mockito.mock(Customer.class));
        var customerFrom = Mockito.mock(Customer.class);
        Mockito.when(customerFrom.getName()).thenReturn("CUSTOMER_FROM");
        Mockito.when(customerFrom.getCode()).thenReturn("CUSTOMER_FROM_CODE");

        Mockito.when(mock.getCustomerFrom()).thenReturn(customerFrom);

        var dto = externalTransferDomainMapper.toDTO(mock);

        Assertions.assertNotNull(dto);
        Assertions.assertEquals("PENDING",dto.status());
        Assertions.assertEquals("CUSTOMER_FROM",dto.customerFrom().name());
        Assertions.assertEquals("CUSTOMER_FROM_CODE",dto.customerFrom().code());
    }

}
