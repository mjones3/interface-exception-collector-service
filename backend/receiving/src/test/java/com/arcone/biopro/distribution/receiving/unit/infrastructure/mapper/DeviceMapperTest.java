package com.arcone.biopro.distribution.receiving.unit.infrastructure.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.arcone.biopro.distribution.receiving.domain.model.Device;
import com.arcone.biopro.distribution.receiving.domain.model.vo.Barcode;
import com.arcone.biopro.distribution.receiving.domain.model.vo.BloodCenterLocation;
import com.arcone.biopro.distribution.receiving.domain.model.vo.DeviceCategory;
import com.arcone.biopro.distribution.receiving.domain.model.vo.DeviceType;
import com.arcone.biopro.distribution.receiving.infrastructure.dto.DeviceCreatedMessage;
import com.arcone.biopro.distribution.receiving.infrastructure.dto.DevicePayload;
import com.arcone.biopro.distribution.receiving.infrastructure.dto.DeviceUpdatedMessage;
import com.arcone.biopro.distribution.receiving.infrastructure.mapper.DeviceMapper;
import com.arcone.biopro.distribution.receiving.infrastructure.persistence.DeviceEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.ZonedDateTime;

class DeviceMapperTest {

    private DeviceMapper mapper;
    private ZonedDateTime testDate;

    @BeforeEach
    void setUp() {
        mapper = DeviceMapper.INSTANCE;
        testDate = ZonedDateTime.now();
    }

    @Test
    void toDomain_FromDeviceCreatedMessage_Success() {
        // Arrange
        DevicePayload payload = createTestDevicePayload();
        DeviceCreatedMessage message = Mockito.mock(DeviceCreatedMessage.class);
        Mockito.when(message.getPayload()).thenReturn(payload);

        // Act
        Device result = mapper.toDomain(message);

        // Assert
        assertDeviceMatchesPayload(result, payload, null);
    }

    @Test
    void toDomain_FromDeviceUpdatedMessage_Success() {
        // Arrange
        Long id = 1L;
        DevicePayload payload = createTestDevicePayload();
        DeviceUpdatedMessage message = Mockito.mock(DeviceUpdatedMessage.class);
        Mockito.when(message.getPayload()).thenReturn(payload);

        // Act
        Device result = mapper.toDomain(id, message);

        // Assert
        assertDeviceMatchesPayload(result, payload, id);
    }

    @Test
    void toEntity_Success() {
        // Arrange
        Device device = createTestDevice();

        // Act
        DeviceEntity result = mapper.toEntity(device);

        // Assert
        assertNotNull(result);
        assertEquals(device.getId(), result.getId());
        assertEquals(device.getSerialNumber(), result.getSerialNumber());
        assertEquals(device.getDeviceCategory().value(), result.getCategory());
        assertEquals(device.getName(), result.getName());
        assertEquals(device.getType().value(), result.getType());
        assertEquals(device.getLocation().code(), result.getLocation());
        assertEquals(device.getActive(), result.getActive());
        assertEquals(device.getCreateDate(), result.getCreateDate());
        assertNotNull(result.getModificationDate());
        assertEquals(device.getBarcode().bloodCenterId(), result.getBloodCenterId());
    }

    @Test
    void toDomain_FromDeviceEntity_Success() {
        // Arrange
        DeviceEntity entity = createTestDeviceEntity();

        // Act
        Device result = mapper.toDomain(entity);

        // Assert
        assertNotNull(result);
        assertEquals(entity.getId(), result.getId());
        assertEquals(entity.getSerialNumber(), result.getSerialNumber());
        assertEquals(entity.getCategory(), result.getDeviceCategory().value());
        assertEquals(entity.getName(), result.getName());
        assertEquals(entity.getType(), result.getType().value());
        assertEquals(entity.getLocation(), result.getLocation().code());
        assertEquals(entity.getActive(), result.getActive());
        assertEquals(entity.getCreateDate(), result.getCreateDate());
        assertNotNull(result.getModificationDate());
        assertEquals(entity.getBloodCenterId(), result.getBarcode().bloodCenterId());
    }

    private DevicePayload createTestDevicePayload() {
        DevicePayload payload = new DevicePayload();
        payload.setDevice(DeviceType.THERMOMETER().value());
        payload.setDeviceCategory(DeviceCategory.TEMPERATURE().value());
        payload.setId("TEST123");
        payload.setSerialNumber("SN123");
        payload.setLocation("LOCATION_1");
        payload.setName("Test Device");
        payload.setStatus("ACTIVE");
        payload.setCreateDate(testDate);
        return payload;
    }

    private Device createTestDevice() {

        var device = Mockito.mock(Device.class);
        Mockito.when(device.getId()).thenReturn(1L);
        Mockito.when(device.getSerialNumber()).thenReturn("SN123");
        Mockito.when(device.getDeviceCategory()).thenReturn(DeviceCategory.TEMPERATURE());
        Mockito.when(device.getName()).thenReturn("Test Device");
        Mockito.when(device.getType()).thenReturn(DeviceType.THERMOMETER());
        Mockito.when(device.getLocation()).thenReturn(new BloodCenterLocation("123"));
        Mockito.when(device.getActive()).thenReturn(true);
        Mockito.when(device.getCreateDate()).thenReturn(testDate);
        Mockito.when(device.getModificationDate()).thenReturn(testDate);
        Mockito.when(device.getBarcode()).thenReturn(new Barcode("TEST123"));

        return device;

    }

    private DeviceEntity createTestDeviceEntity() {
        return DeviceEntity.builder()
            .id(1L)
            .serialNumber("SN123")
            .category(DeviceCategory.TEMPERATURE().value())
            .name("Test Device")
            .type(DeviceType.THERMOMETER().value())
            .location("LOCATION_1")
            .active(true)
            .createDate(testDate)
            .modificationDate(testDate)
            .bloodCenterId("TEST123")
            .build();
    }

    private void assertDeviceMatchesPayload(Device device, DevicePayload payload, Long expectedId) {
        assertNotNull(device);
        assertEquals(expectedId, device.getId());
        assertEquals(payload.getSerialNumber(), device.getSerialNumber());
        assertEquals(payload.getDevice(), device.getType().value());
        assertEquals(payload.getDeviceCategory(), device.getDeviceCategory().value());
        assertEquals(payload.getName(), device.getName());
        assertEquals(payload.getLocation(), device.getLocation().code());
        assertEquals(true, device.getActive());
        assertEquals(payload.getCreateDate(), device.getCreateDate());
        assertNotNull(device.getModificationDate());
        assertEquals(payload.getId(), device.getBarcode().bloodCenterId());
    }



    @Test
    void toDomain_WithNullPayload_ShouldHandleGracefully() {
        // Arrange

        DeviceCreatedMessage message = Mockito.mock(DeviceCreatedMessage.class);

        // Act & Assert
        assertThrows(NullPointerException.class, () -> mapper.toDomain(message));
    }

    @Test
    void toEntity_WithNullDevice_ShouldHandleGracefully() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> mapper.toEntity(null));
    }

    @Test
    void toDomain_WithNullEntity_ShouldHandleGracefully() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> mapper.toDomain((DeviceEntity) null));
    }
}

