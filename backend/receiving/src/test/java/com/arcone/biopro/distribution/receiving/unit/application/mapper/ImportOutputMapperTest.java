package com.arcone.biopro.distribution.receiving.unit.application.mapper;

import com.arcone.biopro.distribution.receiving.application.dto.ImportItemOutput;
import com.arcone.biopro.distribution.receiving.application.dto.ImportOutput;
import com.arcone.biopro.distribution.receiving.application.mapper.ImportOutputMapper;
import com.arcone.biopro.distribution.receiving.domain.model.Import;
import com.arcone.biopro.distribution.receiving.domain.model.ImportItem;
import com.arcone.biopro.distribution.receiving.domain.model.vo.AboRh;
import com.arcone.biopro.distribution.receiving.domain.model.vo.LicenseStatus;
import com.arcone.biopro.distribution.receiving.domain.model.vo.VisualInspection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImportOutputMapperTest {


    private ImportOutputMapper mapper;

    @BeforeEach
    public void setUp(){
        mapper =  Mappers.getMapper(ImportOutputMapper.class);
    }

    @Test
    void toOutput_WhenImportModelIsValid_ShouldMapCorrectly() {
        // Arrange
        Import importModel = createSampleImport();

        // Act
        ImportOutput result = mapper.toOutput(importModel);

        // Assert
        assertNotNull(result);
        assertEquals(importModel.isQuarantined(), result.isQuarantined());
        assertEquals(2, result.products().size());

        // Verify first product mapping
        ImportItemOutput firstProduct = result.products().get(0);
        assertEquals(importModel.getItems().get(0).getVisualInspection().value(), firstProduct.visualInspection());
        assertEquals(importModel.getItems().get(0).getLicenseStatus().value(), firstProduct.licenseStatus());
        assertEquals(importModel.getItems().get(0).getAboRh().description(), firstProduct.aboRh());
    }

    @Test
    void toOutput_WhenImportItemsIsNull_ShouldReturnNullProducts() {
        // Arrange
        var importMock = Mockito.mock(Import.class);
        Mockito.when(importMock.isQuarantined()).thenReturn(true);

        // Act
        ImportOutput result = mapper.toOutput(importMock);

        // Assert
        assertNotNull(result);
        assertTrue(result.isQuarantined());
        assertTrue(result.products().isEmpty());
    }

    @Test
    void toOutput_WhenMappingSingleImportItem_ShouldMapCorrectly() {
        // Arrange
        ImportItem importItem = createSampleImportItem();

        // Act
        ImportItemOutput result = mapper.toOutput(importItem);

        // Assert
        assertNotNull(result);
        assertEquals(importItem.getVisualInspection().value(), result.visualInspection());
        assertEquals(importItem.getLicenseStatus().value(), result.licenseStatus());
        assertEquals(importItem.getAboRh().description(), result.aboRh());
    }

    @Test
    void toOutputList_WhenImportItemListIsNull_ShouldReturnNull() {
        // Act
        List<ImportItemOutput> result = mapper.toOutputList(null);

        // Assert
        assertNull(result);
    }

    @Test
    void toOutputList_WhenImportItemListIsEmpty_ShouldReturnEmptyList() {
        // Act
        List<ImportItemOutput> result = mapper.toOutputList(Collections.emptyList());

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    private Import createSampleImport() {
        var importMock = Mockito.mock(Import.class,Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(importMock.isQuarantined()).thenReturn(true);

        var list = List.of(createSampleImportItem(),createSampleImportItem());
        Mockito.when(importMock.getItems()).thenReturn(list);
        return importMock;


    }

    private ImportItem createSampleImportItem() {
        var importItemMock = Mockito.mock(ImportItem.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(importItemMock.getVisualInspection()).thenReturn(VisualInspection.SATISFACTORY());
        Mockito.when(importItemMock.getLicenseStatus()).thenReturn(LicenseStatus.LICENSED());
        Mockito.when(importItemMock.getAboRh()).thenReturn(AboRh.AP());
        return importItemMock;
    }
}
