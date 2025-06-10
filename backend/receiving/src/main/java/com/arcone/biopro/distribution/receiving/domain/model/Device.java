package com.arcone.biopro.distribution.receiving.domain.model;

import com.arcone.biopro.distribution.receiving.domain.model.vo.Barcode;
import com.arcone.biopro.distribution.receiving.domain.model.vo.BloodCenterLocation;
import com.arcone.biopro.distribution.receiving.domain.model.vo.DeviceCategory;
import com.arcone.biopro.distribution.receiving.domain.model.vo.DeviceType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class Device implements Validatable {

    private Long id;
    private DeviceType type ;
    private DeviceCategory deviceCategory;
    private Barcode barcode;
    private String serialNumber;
    private BloodCenterLocation location;
    private String name;
    private Boolean active;
    private java.time.ZonedDateTime createDate;
    private ZonedDateTime modificationDate;


    public static Device fromEvent(Long id , String deviceType , String deviceCategory
        , String barcode , String serialNumber , String locationCode , String name , String active , ZonedDateTime createDate
        , ZonedDateTime modificationDate){

        var type = DeviceType.getInstance(deviceType);

        if(type == null){
            throw new IllegalArgumentException("Device type is invalid.");
        }

        var category = DeviceCategory.getInstance(deviceCategory);

        if(category == null){
            throw new IllegalArgumentException("Device Category is invalid.");
        }

        var device = Device.builder()
            .id(id)
            .type(type)
            .deviceCategory(category)
            .barcode(new Barcode(barcode))
            .serialNumber(serialNumber)
            .location(new BloodCenterLocation(locationCode))
            .name(name)
            .active("ACTIVE".equals(active))
            .createDate(createDate)
            .modificationDate(modificationDate)
            .build();

        device.checkValid();
        return device;
    }

    public static Device fromRepository(Long id , String deviceType , String deviceCategory
        , String barcode , String serialNumber , String locationCode , String name , Boolean active , ZonedDateTime createDate
        , ZonedDateTime modificationDate){

        var device = Device.builder()
            .id(id)
            .type(DeviceType.getInstance(deviceType))
            .deviceCategory(DeviceCategory.getInstance(deviceCategory))
            .barcode(new Barcode(barcode))
            .serialNumber(serialNumber)
            .location(new BloodCenterLocation(locationCode))
            .name(name)
            .active(active)
            .createDate(createDate)
            .modificationDate(modificationDate)
            .build();

        device.checkValid();
        return device;
    }

    @Override
    public void checkValid() {
        if(type == null){
            throw new IllegalArgumentException("Device type is invalid.");
        }

        if(deviceCategory == null){
            throw new IllegalArgumentException("Device Category is invalid.");
        }

        if(barcode == null){
            throw new IllegalArgumentException("Barcode is required.");
        }

        if(serialNumber == null || serialNumber.isBlank()){
            throw new IllegalArgumentException("Serial number is required.");
        }

        if(location == null){
            throw new IllegalArgumentException("Location is required.");
        }

        if(name == null || name.isBlank()){
            throw new IllegalArgumentException("Name is required.");
        }
    }
}
