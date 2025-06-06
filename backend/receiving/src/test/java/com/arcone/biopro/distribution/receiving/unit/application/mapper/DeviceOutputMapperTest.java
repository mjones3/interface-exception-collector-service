package com.arcone.biopro.distribution.receiving.unit.application.mapper;

import com.arcone.biopro.distribution.receiving.application.dto.DeviceOutput;
import com.arcone.biopro.distribution.receiving.application.mapper.DeviceOutputMapper;
import com.arcone.biopro.distribution.receiving.application.mapper.DeviceOutputMapperImpl;
import com.arcone.biopro.distribution.receiving.domain.model.Device;
import com.arcone.biopro.distribution.receiving.domain.model.vo.Barcode;
import com.arcone.biopro.distribution.receiving.domain.model.vo.BloodCenterLocation;
import com.arcone.biopro.distribution.receiving.domain.model.vo.DeviceCategory;
import com.arcone.biopro.distribution.receiving.domain.model.vo.DeviceType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DeviceOutputMapperImpl.class})
class DeviceOutputMapperTest {

    @Autowired
    private DeviceOutputMapper deviceOutputMapper;

    @Test
    void toOutput_WhenDeviceIsComplete_ShouldMapAllFields() {
        // Arrange

        var device = Mockito.mock(Device.class);
        Mockito.when(device.getId()).thenReturn(1L);
        Mockito.when(device.getSerialNumber()).thenReturn("SN123");
        Mockito.when(device.getDeviceCategory()).thenReturn(DeviceCategory.TEMPERATURE());
        Mockito.when(device.getName()).thenReturn("Test Device");
        Mockito.when(device.getType()).thenReturn(DeviceType.THERMOMETER());
        Mockito.when(device.getLocation()).thenReturn(new BloodCenterLocation("123"));
        Mockito.when(device.getActive()).thenReturn(true);
        Mockito.when(device.getCreateDate()).thenReturn(ZonedDateTime.now());
        Mockito.when(device.getModificationDate()).thenReturn(ZonedDateTime.now());
        Mockito.when(device.getBarcode()).thenReturn(new Barcode("TEST123"));

        // Act
        DeviceOutput output = deviceOutputMapper.toOutput(device);

        // Assert
        assertNotNull(output);
        assertEquals("TEST123", output.bloodCenterId());
        assertEquals("SN123", output.serialNumber());
        assertEquals(DeviceCategory.TEMPERATURE().value(), output.deviceCategory());
        assertEquals("Test Device", output.name());
        assertEquals(DeviceType.THERMOMETER().value(), output.deviceType());
        assertEquals("123", output.locationCode());

    }

    @Test
    void toOutput_WhenDeviceHasNullValues_ShouldMapNonNullFields() {
        // Arrange
        var device = Mockito.mock(Device.class);
        Mockito.when(device.getId()).thenReturn(1L);
        Mockito.when(device.getDeviceCategory()).thenReturn(DeviceCategory.TEMPERATURE());
        Mockito.when(device.getType()).thenReturn(DeviceType.THERMOMETER());
        Mockito.when(device.getLocation()).thenReturn(new BloodCenterLocation("123"));
        Mockito.when(device.getActive()).thenReturn(true);
        Mockito.when(device.getCreateDate()).thenReturn(ZonedDateTime.now());
        Mockito.when(device.getModificationDate()).thenReturn(ZonedDateTime.now());
        Mockito.when(device.getBarcode()).thenReturn(new Barcode("TEST123"));

        // Act
        DeviceOutput output = deviceOutputMapper.toOutput(device);

        // Assert
        assertNotNull(output);
        assertEquals("TEST123", output.bloodCenterId());
        assertEquals(DeviceCategory.TEMPERATURE().value(), output.deviceCategory());
        assertEquals(DeviceType.THERMOMETER().value(), output.deviceType());
        assertEquals("123", output.locationCode());
        assertNull(output.serialNumber());
        assertNull(output.name());
    }

    @Test
    void toOutput_WhenDeviceIsNull_ShouldReturnNull() {
        // Act
        DeviceOutput output = deviceOutputMapper.toOutput(null);

        // Assert
        assertNull(output);
    }
}
