package com.arcone.biopro.distribution.recoveredplasmashipping.unit.infrastructure.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.UnacceptableUnitReportItem;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper.UnacceptableUnitReportEntityMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.UnacceptableUnitReportEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;


class UnacceptableUnitReportEntityMapperTest {


    private UnacceptableUnitReportEntityMapper mapper;


    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(UnacceptableUnitReportEntityMapper.class);
    }


    @Test
    void shouldMapAllFieldsToEntity() {
        // Given
        UnacceptableUnitReportItem item = createCompleteItem();

        // When
        UnacceptableUnitReportEntity entity = mapper.toEntity(item);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getUnitNumber()).isEqualTo(item.getUnitNumber());
        assertThat(entity.getShipmentId()).isEqualTo(item.getShipmentId());
        assertThat(entity.getCartonNumber()).isEqualTo(item.getCartonNumber());
        assertThat(entity.getUnitNumber()).isEqualTo(item.getUnitNumber());
        assertThat(entity.getProductCode()).isEqualTo(item.getProductCode());
        assertThat(entity.getFailureReason()).isEqualTo(item.getFailureReason());
        assertThat(entity.getCreateDate()).isEqualTo(item.getCreateDate());

    }

    @Test
    void shouldMapAllFieldsToModel() {
        // Given
        UnacceptableUnitReportEntity entity = createCompleteEntity();

        // When
        UnacceptableUnitReportItem item = mapper.toModel(entity);

        // Then
        assertThat(item).isNotNull();
        assertThat(item.getUnitNumber()).isEqualTo(entity.getUnitNumber());
        assertThat(item.getShipmentId()).isEqualTo(entity.getShipmentId());
        assertThat(item.getCartonNumber()).isEqualTo(entity.getCartonNumber());
        assertThat(item.getCartonSequenceNumber()).isEqualTo(entity.getCartonSequenceNumber());
        assertThat(item.getFailureReason()).isEqualTo(entity.getFailureReason());
        assertThat(item.getCreateDate()).isEqualTo(entity.getCreateDate());
    }

    @Test
    void shouldHandleNullInput() {
        // When
        UnacceptableUnitReportEntity entity = mapper.toEntity(null);
        UnacceptableUnitReportItem item = mapper.toModel(null);

        // Then
        assertThat(entity).isNull();
        assertThat(item).isNull();
    }

    private UnacceptableUnitReportItem createCompleteItem() {
        return  new UnacceptableUnitReportItem(1L,"CARTON_NUMBER",1
            ,"UNIT_NUMBER","PRODUCT_CODE","FAILURE_REASON", ZonedDateTime.now());

    }

    private UnacceptableUnitReportEntity createCompleteEntity() {
        return UnacceptableUnitReportEntity
            .builder()
            .id(1L)
            .shipmentId(1L)
            .cartonNumber("CARTON_NUMBER")
            .cartonSequenceNumber(1)
            .unitNumber("UNIT_NUMBER")
            .createDate(ZonedDateTime.now())
            .productCode("PRODUCT_CODE")
            .failureReason("FAILURE_REASON")
            .build();

    }

    @Test
    void shouldPreserveAllFieldsInBidirectionalMapping() {
        // Given
        UnacceptableUnitReportItem originalItem = createCompleteItem();

        // When
        UnacceptableUnitReportEntity entity = mapper.toEntity(originalItem);
        UnacceptableUnitReportItem mappedBackItem = mapper.toModel(entity);

        // Then
        assertThat(mappedBackItem)
            .usingRecursiveComparison()
            .isEqualTo(originalItem);
    }
}

