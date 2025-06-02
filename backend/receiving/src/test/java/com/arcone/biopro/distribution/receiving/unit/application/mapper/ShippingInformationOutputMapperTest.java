package com.arcone.biopro.distribution.receiving.unit.application.mapper;

import com.arcone.biopro.distribution.receiving.application.dto.ShippingInformationOutput;
import com.arcone.biopro.distribution.receiving.application.mapper.ShippingInformationOutputMapper;
import com.arcone.biopro.distribution.receiving.domain.model.ShippingInformation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;

@ExtendWith(MockitoExtension.class)
class ShippingInformationOutputMapperTest {


    private ShippingInformationOutputMapper mapper;

    @BeforeEach
    void setUp() {
        // MapStruct generates the implementation class
        mapper = Mappers.getMapper(ShippingInformationOutputMapper.class);
    }

    @Test
    void shouldMapToOutputSuccessfully() {
        // Given
        ShippingInformation shippingInformation = createSampleShippingInformation();

        // When
        ShippingInformationOutput result = mapper.mapToOutput(shippingInformation);

        // Then
        assertNotNull(result);
        verifyMappedFields(result, shippingInformation);

    }

    @Test
    void shouldReturnNullWhenInputIsNull() {
        // When
        ShippingInformationOutput result = mapper.mapToOutput(null);

        // Then
        assertNull(result);
    }

    private ShippingInformation createSampleShippingInformation() {
        ShippingInformation info = Mockito.mock(ShippingInformation.class, RETURNS_DEEP_STUBS);
        Mockito.when(info.getProductCategory()).thenReturn("CATEGORY");
        Mockito.when(info.getTemperatureUnit()).thenReturn("UNIT");
        Mockito.when(info.isDisplayTransitInformation()).thenReturn(true);
        Mockito.when(info.isDisplayTemperature()).thenReturn(true);

        return info;
    }

    private void verifyMappedFields(ShippingInformationOutput output, ShippingInformation shippingInformation) {
        assertNotNull(output);
        assertEquals(shippingInformation.getProductCategory(),output.productCategory());
        assertEquals(shippingInformation.getTemperatureUnit(),output.temperatureUnit());
        assertEquals(shippingInformation.isDisplayTransitInformation(),output.displayTransitInformation());
        assertEquals(shippingInformation.isDisplayTemperature(),output.displayTemperature());
    }
}

