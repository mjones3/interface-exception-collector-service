package com.arcone.biopro.distribution.recoveredplasmashipping.unit.application.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CartonOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CartonPackingSlipOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.CartonItemOutputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.CartonItemOutputMapperImpl;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.CartonOutputMapperImpl;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.PackingSlipProductMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.PackingSlipProductMapperImpl;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Carton;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.CartonItem;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.CartonPackingSlip;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Inventory;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.InventoryValidation;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.PackingSlipShipFrom;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.PackingSlipShipTo;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.PackingSlipShipment;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(classes = {CartonOutputMapperImpl.class, CartonItemOutputMapperImpl.class , PackingSlipProductMapperImpl.class})
class CartonOutputMapperTest {

        @Mock
        private CartonItemOutputMapper cartonItemOutputMapper;

        @Mock
        private PackingSlipProductMapper packingSlipProductMapper;

        @InjectMocks
        private CartonOutputMapperImpl cartonOutputMapper;

        @Test
        void toOutput_ShouldMapCartonToCartonOutput() {
            // Arrange
            Carton carton = Mockito.mock(Carton.class);
            Mockito.when(carton.canClose()).thenReturn(true);
            Mockito.when(carton.canVerify()).thenReturn(true);

            CartonItem cartonItem = Mockito.mock(CartonItem.class);
            Mockito.when(cartonItem.getStatus()).thenReturn("VERIFIED");
            Mockito.when(carton.getVerifiedProducts()).thenReturn(List.of(cartonItem));

            // Act
            CartonOutput result = cartonOutputMapper.toOutput(carton);

            // Assert
            assertNotNull(result);
            assertTrue(result.canClose());
            assertTrue(result.canVerify());
            assertNotNull(result.verifiedProducts());
            assertTrue(result.verifiedProducts().size() > 0);
        }


        @Test
        void toOutput_shouldMapCartonToCartonOutput() {
            // Given
            Carton carton = Mockito.mock(Carton.class);

            CartonItem cartonItem = Mockito.mock(CartonItem.class);

            Mockito.when(carton.getProducts()).thenReturn(List.of(cartonItem));

            when(carton.canVerify()).thenReturn(true);
            when(carton.canClose()).thenReturn(false);

            // When
            CartonOutput result = cartonOutputMapper.toOutput(carton);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.packedProducts().size()).isEqualTo(1);
            assertThat(result.canVerify()).isTrue();
            assertThat(result.canClose()).isFalse();
        }

        @Test
        void toOutput_withInventoryValidation_shouldMapToCartonOutput() {
            // Given
            Carton carton = Mockito.mock(Carton.class);
            CartonItem cartonItem = Mockito.mock(CartonItem.class);

            Mockito.when(carton.getProducts()).thenReturn(List.of(cartonItem));

            when(carton.canVerify()).thenReturn(true);
            when(carton.canClose()).thenReturn(false);

            Inventory inventory = Mockito.mock(Inventory.class);
            Mockito.when(inventory.getUnitNumber()).thenReturn("UN123");
            Mockito.when(inventory.getProductCode()).thenReturn("PC456");
            Mockito.when(inventory.getProductDescription()).thenReturn("Test Product");
            Mockito.when(inventory.getProductFamily()).thenReturn("Test Family");
            Mockito.when(inventory.getWeight()).thenReturn(10);
            Mockito.when(inventory.getAboRh()).thenReturn("AP");
            Mockito.when(inventory.getExpirationDate()).thenReturn(LocalDateTime.now().plusDays(30));
            Mockito.when(inventory.getCollectionDate()).thenReturn(ZonedDateTime.now().minusDays(1));
            Mockito.when(inventory.getCreateDate()).thenReturn(ZonedDateTime.now());
            Mockito.when(inventory.getModificationDate()).thenReturn(ZonedDateTime.now());

            InventoryValidation inventoryValidation = Mockito.mock(InventoryValidation.class);
            Mockito.when(inventoryValidation.getInventory()).thenReturn(inventory);

            // When
            CartonOutput result = cartonOutputMapper.toOutput(carton, inventoryValidation);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.packedProducts().size()).isEqualTo(1);
            assertThat(result.canVerify()).isTrue();
            assertThat(result.canClose()).isFalse();

            assertThat(result.failedCartonItem()).isNotNull();
            assertThat(result.failedCartonItem().unitNumber()).isEqualTo("UN123");
            assertThat(result.failedCartonItem().productCode()).isEqualTo("PC456");
            assertThat(result.failedCartonItem().productDescription()).isEqualTo("Test Product");
            assertThat(result.failedCartonItem().productType()).isEqualTo("Test Family");
            assertThat(result.failedCartonItem().weight()).isEqualTo(10);
            assertThat(result.failedCartonItem().aboRh()).isEqualTo("AP");
        }

        @Test
        void toOutput_withCartonPackingSlip_shouldMapToCartonPackingSlipOutput() {
            // Given
            CartonPackingSlip cartonPackingSlip = Mockito.mock(CartonPackingSlip.class);
            Mockito.when(cartonPackingSlip.getCartonId()).thenReturn(1L);
            Mockito.when(cartonPackingSlip.getCartonNumber()).thenReturn("CN456");
            Mockito.when(cartonPackingSlip.getCartonSequence()).thenReturn(1);
            Mockito.when(cartonPackingSlip.getTotalProducts()).thenReturn(10);
            Mockito.when(cartonPackingSlip.getDateTimePacked()).thenReturn("DATE");
            Mockito.when(cartonPackingSlip.getPackedByEmployeeId()).thenReturn("EMP789");
            Mockito.when(cartonPackingSlip.getTestingStatement()).thenReturn("Test Statement");
            Mockito.when(cartonPackingSlip.isDisplaySignature()).thenReturn(true);
            Mockito.when(cartonPackingSlip.isDisplayTransportationReferenceNumber()).thenReturn(true);
            Mockito.when(cartonPackingSlip.isDisplayTestingStatement()).thenReturn(true);
            Mockito.when(cartonPackingSlip.isDisplayLicenceNumber()).thenReturn(true);

            PackingSlipShipFrom shipFrom = Mockito.mock(PackingSlipShipFrom.class);
            Mockito.when(shipFrom.getBloodCenterName()).thenReturn("Blood Center");
            Mockito.when(shipFrom.getLicenseNumber()).thenReturn("L123");
            Mockito.when(shipFrom.getLocationAddressFormatted()).thenReturn("123 Test St");
            Mockito.when(cartonPackingSlip.getShipFrom()).thenReturn(shipFrom);



            PackingSlipShipTo shipTo = Mockito.mock(PackingSlipShipTo.class);
            Mockito.when(shipTo.getFormattedAddress()).thenReturn("456 Dest St");
            Mockito.when(cartonPackingSlip.getShipTo()).thenReturn(shipTo);



            PackingSlipShipment shipment = Mockito.mock(PackingSlipShipment.class);
            Mockito.when(shipment.getShipmentNumber()).thenReturn("SH123");
            Mockito.when(shipment.getProductType()).thenReturn("Type A");
            Mockito.when(shipment.getProductDescription()).thenReturn("Description");
            Mockito.when(shipment.getTransportationReferenceNumber()).thenReturn("TRN123");
            Mockito.when(cartonPackingSlip.getPackingSlipShipment()).thenReturn(shipment);

            Mockito.when(cartonPackingSlip.getCartonProductCode()).thenReturn("CARTON_PRODUCT_CODE");
            Mockito.when(cartonPackingSlip.getCartonProductDescription()).thenReturn("CARTON_PRODUCT_DESC");


            // When
            CartonPackingSlipOutput result = cartonOutputMapper.toOutPut(cartonPackingSlip);

            // Then
            assertThat(result).isNotNull();

            assertThat(result.cartonId()).isEqualTo(1L);
            assertThat(result.cartonSequence()).isEqualTo(1);
            assertThat(result.totalProducts()).isEqualTo(10);
            assertThat(result.packedByEmployeeId()).isEqualTo("EMP789");
            assertThat(result.testingStatement()).isEqualTo("Test Statement");
            assertThat(result.displaySignature()).isTrue();
            assertThat(result.displayTransportationReferenceNumber()).isTrue();
            assertThat(result.displayTestingStatement()).isTrue();
            assertThat(result.displayLicenceNumber()).isTrue();
            assertThat(result.shipFromBloodCenterName()).isEqualTo("Blood Center");
            assertThat(result.shipFromLicenseNumber()).isEqualTo("L123");
            assertThat(result.shipFromLocationAddress()).isEqualTo("123 Test St");
            assertThat(result.shipToAddress()).isEqualTo("456 Dest St");
            assertThat(result.shipmentNumber()).isEqualTo("SH123");
            assertThat(result.shipmentProductType()).isEqualTo("Type A");
            assertThat(result.shipmentProductDescription()).isEqualTo("Description");
            assertThat(result.shipmentTransportationReferenceNumber()).isEqualTo("TRN123");
            assertThat(result.cartonProductCode()).isEqualTo("CARTON_PRODUCT_CODE");
            assertThat(result.cartonProductDescription()).isEqualTo("CARTON_PRODUCT_DESC");
        }
}
