package com.arcone.biopro.distribution.receiving.application.mapper;

import com.arcone.biopro.distribution.receiving.application.dto.ImportItemOutput;
import com.arcone.biopro.distribution.receiving.application.dto.ImportOutput;
import com.arcone.biopro.distribution.receiving.domain.model.Import;
import com.arcone.biopro.distribution.receiving.domain.model.ImportItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ImportOutputMapper {

    @Mapping(expression ="java(importModel.isQuarantined())" , target = "isQuarantined")
    @Mapping(expression ="java(toOutputList(importModel.getItems()))" , target = "products")
    ImportOutput toOutput(Import importModel);

    @Mapping(expression ="java(importItem.getVisualInspection().value())" , target = "licenseStatus")
    @Mapping(expression ="java(importItem.getLicenseStatus().value())" , target = "visualInspection")
    @Mapping(expression ="java(importItem.getAboRh().description())" , target = "aboRh")
    ImportItemOutput toOutput(ImportItem importItem);

    default  List<ImportItemOutput> toOutputList(List<ImportItem> importItemList) {
        if(importItemList == null){
            return null;
        }
        return importItemList.stream().map(this::toOutput).toList();
    }
}
