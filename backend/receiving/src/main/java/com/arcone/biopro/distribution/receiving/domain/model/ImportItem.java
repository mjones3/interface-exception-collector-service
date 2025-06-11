package com.arcone.biopro.distribution.receiving.domain.model;

import com.arcone.biopro.distribution.receiving.domain.model.vo.AboRh;
import com.arcone.biopro.distribution.receiving.domain.model.vo.ImportItemConsequence;
import com.arcone.biopro.distribution.receiving.domain.model.vo.LicenseStatus;
import com.arcone.biopro.distribution.receiving.domain.model.vo.VisualInspection;
import com.arcone.biopro.distribution.receiving.domain.service.ConfigurationService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class ImportItem implements Validatable {

    private final Long id;
    private final Long importId;
    private final VisualInspection visualInspection;
    private final LicenseStatus licenseStatus;
    private final String unitNumber;
    private final String productCode;
    private final AboRh aboRh;
    private final LocalDateTime expirationDate;
    private final String productFamily;
    private final String productDescription;
    private final ZonedDateTime createDate;
    private final ZonedDateTime modificationDate;
    private final String employeeId;
    private Map<String,String> properties;
    private List<ImportItemConsequence> consequences;

    public static ImportItem create(AddImportItemCommand command , ConfigurationService configurationService, List<ImportItemConsequence> consequenceList) {

        if(command == null) {
            throw new IllegalArgumentException("Command is required");
        }

        var product = getProductDetails(command.getProductCode(), configurationService);

        var importItem = ImportItem.builder()
            .id(null)
            .importId(command.getImportId())
            .visualInspection(command.getVisualInspection())
            .licenseStatus(command.getLicenseStatus())
            .unitNumber(command.getUnitNumber())
            .productCode(command.getProductCode())
            .aboRh(command.getAboRh())
            .expirationDate(command.getExpirationDate())
            .productFamily(product.getProductFamily())
            .productDescription(product.getShortDescription())
            .createDate(ZonedDateTime.now())
            .modificationDate(ZonedDateTime.now())
            .employeeId(command.getEmployeeId())
            .consequences(consequenceList)
            .properties(getItemProperties(command))
            .build();

        importItem.checkValid();

        return importItem;
    }


    public static ImportItem fromRepository(Long id, Long importId,String unitNumber, String productCode, String aboRh
        , LocalDateTime expirationDate, String productFamily, String productDescription, ZonedDateTime createDate, ZonedDateTime modificationDate
        , String employeeId, Map<String,String> properties , List<ImportItemConsequence> consequences) {

        var importItem = ImportItem.builder()
            .id(id)
            .importId(importId)
            .unitNumber(unitNumber)
            .productCode(productCode)
            .aboRh(AboRh.getInstance(aboRh))
            .expirationDate(expirationDate)
            .productFamily(productFamily)
            .productDescription(productDescription)
            .createDate(createDate)
            .modificationDate(modificationDate)
            .employeeId(employeeId)
            .properties(properties)
            .consequences(consequences)
            .build();

        importItem.checkValid();

        return importItem;


    }

    @Override
    public void checkValid() {

        if (importId == null) {
            throw new IllegalArgumentException("Import Id is required");
        }
        if (getVisualInspection() == null) {
            throw new IllegalArgumentException("Visual Inspection is required");
        }
        if (getLicenseStatus() == null) {
            throw new IllegalArgumentException("License Status is required");
        }
        if (unitNumber == null || unitNumber.isBlank()) {
            throw new IllegalArgumentException("Unit Number is required");
        }
        if (productCode == null || productCode.isBlank()) {
            throw new IllegalArgumentException("Product Code is required");
        }
        if (aboRh == null) {
            throw new IllegalArgumentException("ABO/RH is required");
        }
        if (expirationDate == null) {
            throw new IllegalArgumentException("Expiration Date is required");
        }

        if (productFamily == null || productFamily.isBlank()) {
            throw new IllegalArgumentException("Product Family is required");
        }

        if (productDescription == null || productDescription.isBlank()) {
            throw new IllegalArgumentException("Product Description is required");
        }

    }


    public boolean isQuarantined(){
        return consequences != null && !consequences.isEmpty();
    }

    public LicenseStatus getLicenseStatus(){
        if(properties != null && properties.containsKey("LICENSE_STATUS")){
            return LicenseStatus.getInstance(properties.get("LICENSE_STATUS"));
        }
        return null;
    }

    public VisualInspection getVisualInspection(){
        if(properties != null && properties.containsKey("VISUAL_INSPECTION")){
            return VisualInspection.getInstance(properties.get("VISUAL_INSPECTION"));
        }
        return null;
    }

    private static Map<String,String> getItemProperties(AddImportItemCommand addImportItemCommand){
        if(addImportItemCommand == null){
            throw new IllegalArgumentException("Add Command is required");
        }

        var properties = new HashMap<String,String>();

        if (addImportItemCommand.getLicenseStatus() != null) {
            properties.put("LICENSE_STATUS",addImportItemCommand.getLicenseStatus().value());
        }

        if (addImportItemCommand.getVisualInspection() != null) {
            properties.put("VISUAL_INSPECTION",addImportItemCommand.getVisualInspection().value());
        }

        return properties;
    }

    private static Product getProductDetails(String productCode , ConfigurationService configurationService){
        if(configurationService == null){
            log.error("Configuration Service is Null");
            throw new IllegalArgumentException("ConfigurationService is required");
        }

        return configurationService.findProductByCode(productCode)
            .switchIfEmpty(Mono.error(new IllegalArgumentException("Product Details not found.")))
            .block();
    }
}
