package com.arcone.biopro.distribution.receiving.unit.infrastructure.mapper;

import com.arcone.biopro.distribution.receiving.domain.model.Import;
import com.arcone.biopro.distribution.receiving.domain.model.ImportItem;
import com.arcone.biopro.distribution.receiving.domain.model.vo.AboRh;
import com.arcone.biopro.distribution.receiving.domain.model.vo.ImportItemConsequence;
import com.arcone.biopro.distribution.receiving.infrastructure.dto.ProductsImportedPayload;
import com.arcone.biopro.distribution.receiving.infrastructure.mapper.ProductsImportedOutputMapperImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
class ProductsImportedOutputMapperTest {

    @InjectMocks
    private ProductsImportedOutputMapperImpl mapper;

    @BeforeEach
    void setUp() {
        mapper = new ProductsImportedOutputMapperImpl();
    }

    @Test
    void toOutput_ShouldMapImportModelToPayload() {
        // Given
        Import importModel = createSampleImport();

        // When
        ProductsImportedPayload result = mapper.toOutput(importModel);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getProducts()).hasSize(2);
        assertThat(result.getCreateDate()).isEqualTo(importModel.getCreateDate());
        assertThat(result.getLocationCode()).isEqualTo(importModel.getLocationCode());
        assertThat(result.getCreateEmployeeId()).isEqualTo(importModel.getEmployeeId());
        assertThat(result.getTemperature()).isEqualTo(importModel.getTemperature());
        assertThat(result.getComments()).isEqualTo(importModel.getComments());
        assertThat(result.getTemperatureUnit()).isEqualTo("celsius");
        assertThat(result.getTransitTime()).isEqualTo(importModel.getTotalTransitTime());
        assertThat(result.getThermometerCode()).isEqualTo(importModel.getThermometerCode());


        assertThat(result.getProducts().get(0).getUnitNumber()).isEqualTo(importModel.getItems().get(0).getUnitNumber());
        assertThat(result.getProducts().get(0).getExpirationDate()).isEqualTo(importModel.getItems().get(0).getExpirationDate());
        assertThat(result.getProducts().get(0).getAboRh()).isEqualTo(importModel.getItems().get(0).getAboRh().value());

        assertThat(result.getProducts().get(1).getUnitNumber()).isEqualTo(importModel.getItems().get(1).getUnitNumber());
        assertThat(result.getProducts().get(1).getExpirationDate()).isEqualTo(importModel.getItems().get(1).getExpirationDate());
        assertThat(result.getProducts().get(1).getAboRh()).isEqualTo(importModel.getItems().get(1).getAboRh().value());

        assertThat(result.getProducts().get(0).getProperties()).isEqualTo(importModel.getItems().get(0).getProperties());
        assertThat(result.getProducts().get(1).getProperties()).isEqualTo(importModel.getItems().get(1).getProperties());

        assertThat(result.getProducts().get(0).getConsequences().get(0).getConsequenceType()).isEqualTo(importModel.getItems().get(0).getConsequences().get(0).consequenceType());
        Assertions.assertEquals(List.of("REASON"),result.getProducts().get(0).getConsequences().get(0).getConsequenceReasons());
        Assertions.assertEquals(List.of("REASON"),result.getProducts().get(1).getConsequences().get(0).getConsequenceReasons());


    }

    @Test
    void toOutputProduct_ShouldMapImportItemToImportedProduct() {
        // Given
        ImportItem importItem = createSampleImportItem();

        // When
        ProductsImportedPayload.ImportedProduct result = mapper.toOutputProduct(importItem);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUnitNumber()).isEqualTo(importItem.getUnitNumber());
        assertThat(result.getAboRh()).isEqualTo(importItem.getAboRh().value());
        assertThat(result.getExpirationDate()).isEqualTo(importItem.getExpirationDate());
    }

    @Test
    void toOutput_WithNullImport_ShouldReturnNull() {
        // When
        ProductsImportedPayload result = mapper.toOutput(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toOutput_WithEmptyItems_ShouldReturnPayloadWithEmptyProducts() {
        // Given
        Import importModel = Mockito.mock(Import.class);
        Mockito.when(importModel.getCreateDate()).thenReturn(ZonedDateTime.now());


        // When
        ProductsImportedPayload result = mapper.toOutput(importModel);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getProducts()).isEmpty();
    }

    @Test
    void toOutputProduct_WithNullImportItem_ShouldReturnNull() {
        // When
        ProductsImportedPayload.ImportedProduct result = mapper.toOutputProduct(null);

        // Then
        assertThat(result).isNull();
    }

    private Import createSampleImport() {
        var importMock = Mockito.mock(Import.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(importMock.getCreateDate()).thenReturn(ZonedDateTime.now());
        Mockito.when(importMock.getLocationCode()).thenReturn("LOC1");
        Mockito.when(importMock.getEmployeeId()).thenReturn("EMP1");
        Mockito.when(importMock.getTemperatureCategory()).thenReturn("TEMP_CAT");
        Mockito.when(importMock.getTemperature()).thenReturn(BigDecimal.TEN);
        Mockito.when(importMock.getThermometerCode()).thenReturn("THERM1");
        Mockito.when(importMock.getComments()).thenReturn("COMMENT1");
        Mockito.when(importMock.getTotalTransitTime()).thenReturn("10h 30m");

        var items =  List.of(createSampleImportItem(), createSampleImportItem());

        Mockito.when(importMock.getItems()).thenReturn(items);
        return importMock;


    }

    private ImportItem createSampleImportItem() {
        var itemMock = Mockito.mock(ImportItem.class , Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(itemMock.getUnitNumber()).thenReturn("123");
        Mockito.when(itemMock.getExpirationDate()).thenReturn(LocalDateTime.now());
        Mockito.when(itemMock.getAboRh()).thenReturn(AboRh.AP());

        Mockito.when(itemMock.getProperties()).thenReturn(Map.of("KEY_1","VALUE_1"));

        var consequence = ImportItemConsequence
            .builder()
            .consequenceType("TYPE")
            .consequenceReason("REASON")
            .build();

        Mockito.when(itemMock.getConsequences()).thenReturn(List.of(consequence));

        return itemMock;


    }
}



