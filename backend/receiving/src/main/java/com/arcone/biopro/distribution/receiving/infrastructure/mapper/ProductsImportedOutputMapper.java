package com.arcone.biopro.distribution.receiving.infrastructure.mapper;

import com.arcone.biopro.distribution.receiving.domain.model.Import;
import com.arcone.biopro.distribution.receiving.domain.model.ImportItem;
import com.arcone.biopro.distribution.receiving.domain.model.vo.ImportItemConsequence;
import com.arcone.biopro.distribution.receiving.infrastructure.dto.ProductsImportedPayload;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring" , unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductsImportedOutputMapper {

  @Mapping(source ="items" , target = "products")
  @Mapping(source ="employeeId" , target = "createEmployeeId")
  @Mapping(constant ="celsius" , target = "temperatureUnit")
  @Mapping(source = "totalTransitTime", target = "transitTime")
  ProductsImportedPayload toOutput(Import importModel);

  @Mapping(expression ="java(importItemModel.getAboRh().value())" , target = "aboRh")
  ProductsImportedPayload.ImportedProduct toOutputProduct(ImportItem importItemModel);

  @Mapping(expression ="java(List.of(importItemConsequence.consequenceReason()))" , target = "consequenceReasons")
  ProductsImportedPayload.ImportedConsequence toOutputConsequence(ImportItemConsequence importItemConsequence);

}
