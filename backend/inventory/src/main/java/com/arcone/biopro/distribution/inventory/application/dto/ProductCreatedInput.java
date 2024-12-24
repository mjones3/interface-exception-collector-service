package com.arcone.biopro.distribution.inventory.application.dto;

import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.inventory.domain.model.vo.InputProduct;

import java.time.ZonedDateTime;
import java.util.List;

public record ProductCreatedInput(String unitNumber,
                                  String productCode,
                                  String productDescription,
                                  String expirationDate,
                                  String expirationTime,
                                  String expirationTimeZone,
                                  Integer weight,
                                  ZonedDateTime collectionDate,
                                  String location,
                                  String productFamily,
                                  AboRhType aboRh,
                                  List<InputProduct> inputProducts) {
}
