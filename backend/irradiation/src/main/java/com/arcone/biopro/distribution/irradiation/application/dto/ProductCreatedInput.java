package com.arcone.biopro.distribution.irradiation.application.dto;

import com.arcone.biopro.distribution.irradiation.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.irradiation.domain.model.vo.InputProduct;
import lombok.Builder;

import java.time.ZonedDateTime;
import java.util.List;

@Builder
public record ProductCreatedInput(String unitNumber,
                                  String productCode,
                                  String productDescription,
                                  String expirationDate,
                                  String expirationTime,
                                  String expirationTimeZone,
                                  Integer weight,
                                  ZonedDateTime collectionDate,
                                  String inventoryLocation,
                                  String collectionLocation,
                                  String collectionTimeZone,
                                  String productFamily,
                                  AboRhType aboRh,
                                  Boolean licensed,
                                  String temperatureCategory,
                                  List<InputProduct> inputProducts,
                                  List<AddQuarantineInput> quarantines) {
}
