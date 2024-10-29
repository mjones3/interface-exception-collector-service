package com.arcone.biopro.distribution.shipping.unit.application.mapper;


import com.arcone.biopro.distribution.shipping.application.mapper.ReasonDomainMapper;
import com.arcone.biopro.distribution.shipping.domain.model.Reason;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.test.StepVerifier;

class ReasonDomainMapperTest {

    @Test
    public void shouldMapToDto(){

        var reason = Mockito.mock(Reason.class);
        Mockito.when(reason.getId()).thenReturn(1L);
        Mockito.when(reason.getReasonKey()).thenReturn("reasonKey");
        Mockito.when(reason.getType()).thenReturn("type");
        Mockito.when(reason.getOrderNumber()).thenReturn(1);
        Mockito.when(reason.isActive()).thenReturn(true);
        Mockito.when(reason.isRequireComments()).thenReturn(true);

        var mapper = new ReasonDomainMapper();

        var dto = mapper.mapToDTO(reason);

        Assertions.assertEquals(1L,dto.id());
        Assertions.assertEquals("reasonKey",dto.reasonKey());
        Assertions.assertEquals("type",dto.type());
        Assertions.assertEquals(1,dto.orderNumber());
        Assertions.assertEquals(Boolean.TRUE,dto.active());
        Assertions.assertEquals(Boolean.TRUE,dto.requireComments());
    }

    @Test
    public void shouldMapToFlatMapDto(){

        var reason = Mockito.mock(Reason.class);
        Mockito.when(reason.getId()).thenReturn(1L);
        Mockito.when(reason.getReasonKey()).thenReturn("reasonKey");
        Mockito.when(reason.getType()).thenReturn("type");
        Mockito.when(reason.getOrderNumber()).thenReturn(1);
        Mockito.when(reason.isActive()).thenReturn(true);
        Mockito.when(reason.isRequireComments()).thenReturn(true);

        var mapper = new ReasonDomainMapper();

        var dto = mapper.flatMapToDto(reason);

        StepVerifier.create(dto)
            .consumeNextWith(details -> {
                Assertions.assertEquals(1L,details.id());
                Assertions.assertEquals("reasonKey",details.reasonKey());
                Assertions.assertEquals("type",details.type());
                Assertions.assertEquals(1,details.orderNumber());
                Assertions.assertEquals(Boolean.TRUE,details.active());
                Assertions.assertEquals(Boolean.TRUE,details.requireComments());
            })
            .verifyComplete();
    }

}
