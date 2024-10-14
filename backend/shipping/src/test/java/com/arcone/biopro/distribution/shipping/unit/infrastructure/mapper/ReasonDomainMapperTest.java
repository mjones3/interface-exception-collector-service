package com.arcone.biopro.distribution.shipping.unit.infrastructure.mapper;

import com.arcone.biopro.distribution.shipping.infrastructure.mapper.ReasonMapper;
import com.arcone.biopro.distribution.shipping.infrastructure.persistence.ReasonEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.test.StepVerifier;

class ReasonDomainMapperTest {

    @Test
    public void shouldMapToDomain(){

        var reasonEntity = Mockito.mock(ReasonEntity.class);
        Mockito.when(reasonEntity.getId()).thenReturn(1L);
        Mockito.when(reasonEntity.getReasonKey()).thenReturn("reasonKey");
        Mockito.when(reasonEntity.getType()).thenReturn("type");
        Mockito.when(reasonEntity.getOrderNumber()).thenReturn(1);
        Mockito.when(reasonEntity.isActive()).thenReturn(true);
        Mockito.when(reasonEntity.isRequireComments()).thenReturn(true);

        var mapper = new ReasonMapper();

        var domain = mapper.toDomain(reasonEntity);

        Assertions.assertEquals(1L,domain.getId());
        Assertions.assertEquals("reasonKey",domain.getReasonKey());
        Assertions.assertEquals("type",domain.getType());
        Assertions.assertEquals(1,domain.getOrderNumber());
        Assertions.assertEquals(Boolean.TRUE,domain.isActive());
        Assertions.assertEquals(Boolean.TRUE,domain.isRequireComments());
    }

    @Test
    public void shouldMapToFlatMapDomain(){

        var reasonEntity = Mockito.mock(ReasonEntity.class);
        Mockito.when(reasonEntity.getId()).thenReturn(1L);
        Mockito.when(reasonEntity.getReasonKey()).thenReturn("reasonKey");
        Mockito.when(reasonEntity.getType()).thenReturn("type");
        Mockito.when(reasonEntity.getOrderNumber()).thenReturn(1);
        Mockito.when(reasonEntity.isActive()).thenReturn(true);
        Mockito.when(reasonEntity.isRequireComments()).thenReturn(true);

        var mapper = new ReasonMapper();

        var domain = mapper.flatMapToDomain(reasonEntity);

        StepVerifier.create(domain)
            .consumeNextWith(details -> {
                Assertions.assertEquals(1L,details.getId());
                Assertions.assertEquals("reasonKey",details.getReasonKey());
                Assertions.assertEquals("type",details.getType());
                Assertions.assertEquals(1,details.getOrderNumber());
                Assertions.assertEquals(Boolean.TRUE,details.isActive());
                Assertions.assertEquals(Boolean.TRUE,details.isRequireComments());
            })
            .verifyComplete();
    }

}
