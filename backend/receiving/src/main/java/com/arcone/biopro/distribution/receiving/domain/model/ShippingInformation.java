package com.arcone.biopro.distribution.receiving.domain.model;

import com.arcone.biopro.distribution.receiving.domain.repository.LookupRepository;
import com.arcone.biopro.distribution.receiving.domain.repository.ProductConsequenceRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class ShippingInformation {

   private String productCategory;
   private LocalDate startTransitDate;
   private LocalDate endTransitDate;
   private LocalTime startTransitTime;
   private LocalTime endTransitTime;
   private String startTransitTimeZone;
   private String endTransitTimeZone;
   private Integer temperature;
   private String temperatureUnit;
   private String thermometerCode;
   private String comments;
   private boolean displayTransitInformation;
   private boolean displayTemperature;
   private List<Lookup> transitTimeZoneList;
   private List<Lookup> visualInspectionList;

   public static  ShippingInformation fromNewImportBatch(EnterShippingInformationCommand enterShippingInformationCommand , LookupRepository lookupRepository , ProductConsequenceRepository productConsequenceRepository){

       return ShippingInformation.builder()
               .productCategory(enterShippingInformationCommand.getProductCategory())
               .displayTransitInformation(true)
               .temperatureUnit("celsius")
               .transitTimeZoneList(getTransitTimeZoneList(lookupRepository))
               .visualInspectionList(getVisualInspectionList(lookupRepository))
               .displayTemperature(isTemperatureRequired(enterShippingInformationCommand.getProductCategory(), productConsequenceRepository))
               .build();
   }

   private static List<Lookup> getTransitTimeZoneList(LookupRepository lookupRepository){
       if(lookupRepository == null){
           throw new IllegalArgumentException("LookupRepository is required");
       }
       return lookupRepository.findAllByType("TRANSIT_TIME_ZONE").collectList().block();
   }

   private static List<Lookup> getVisualInspectionList(LookupRepository lookupRepository){
       if(lookupRepository == null){
           throw new IllegalArgumentException("LookupRepository is required");
       }
       return lookupRepository.findAllByType("VISUAL_INSPECTION_STATUS").collectList().block();
   }

   private static boolean isTemperatureRequired(String productCategory, ProductConsequenceRepository productConsequenceRepository){
       if(productCategory == null || productCategory.isBlank()){
           throw new IllegalArgumentException("Product category is required");
       }
       if(productConsequenceRepository == null){
           throw new IllegalArgumentException("ProductConsequenceRepository is required");
       }

       var productConsequence = productConsequenceRepository.findAllByProductCategoryAndResultProperty(productCategory, "TEMPERATURE")
               .collectList()
               .block();

       if(productConsequence == null || productConsequence.isEmpty()){
           return false;
       }

       return true;
   }


}
