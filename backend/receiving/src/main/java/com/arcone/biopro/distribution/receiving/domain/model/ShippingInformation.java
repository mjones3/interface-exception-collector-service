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

import java.util.Collections;
import java.util.List;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class ShippingInformation {

   private String productCategory;
   private String temperatureUnit;
   private boolean displayTransitInformation;
   private boolean displayTemperature;
   private List<Lookup> transitTimeZoneList;
   private List<Lookup> visualInspectionList;

   public static  ShippingInformation fromNewImportBatch(EnterShippingInformationCommand enterShippingInformationCommand , LookupRepository lookupRepository , ProductConsequenceRepository productConsequenceRepository){

       validateProductCategory(enterShippingInformationCommand,productConsequenceRepository);

       var requireTransitInformation = isTransitTimeRequired(enterShippingInformationCommand.getProductCategory(), productConsequenceRepository);

       return ShippingInformation.builder()
               .productCategory(enterShippingInformationCommand.getProductCategory())
               .temperatureUnit("celsius")
               .transitTimeZoneList(requireTransitInformation ? getTransitTimeZoneList(lookupRepository) : Collections.emptyList())
               .visualInspectionList(getVisualInspectionList(lookupRepository))
               .displayTransitInformation(requireTransitInformation)
               .displayTemperature(isTemperatureRequired(enterShippingInformationCommand.getProductCategory(), productConsequenceRepository))
               .build();
   }

   private static void validateProductCategory(EnterShippingInformationCommand enterShippingInformationCommand , ProductConsequenceRepository productConsequenceRepository){
       if(enterShippingInformationCommand == null){
           throw new IllegalArgumentException("EnterShippingInformationCommand is required");
       }
       if(enterShippingInformationCommand.getProductCategory() == null || enterShippingInformationCommand.getProductCategory().isBlank()){
           throw new IllegalArgumentException("Product category is required");
       }

       if(productConsequenceRepository == null){
           throw new IllegalArgumentException("ProductConsequenceRepository is required");
       }

       var productConsequence = productConsequenceRepository.findAllByProductCategory(enterShippingInformationCommand.getProductCategory())
           .collectList()
           .block();

       if(productConsequence == null || productConsequence.isEmpty()){
           throw new IllegalArgumentException("Product category is not configured");
       }
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
       return isProductConsequenceConfigured(productCategory,"TEMPERATURE",productConsequenceRepository);
   }

   private static boolean isTransitTimeRequired(String productCategory, ProductConsequenceRepository productConsequenceRepository){
       return isProductConsequenceConfigured(productCategory,"TRANSIT_TIME",productConsequenceRepository);
   }

   private static boolean isProductConsequenceConfigured(String productCategory , String property , ProductConsequenceRepository productConsequenceRepository){
       if(productCategory == null || productCategory.isBlank()){
           throw new IllegalArgumentException("Product category is required");
       }
       if(productConsequenceRepository == null){
           throw new IllegalArgumentException("ProductConsequenceRepository is required");
       }

       var productConsequence = productConsequenceRepository.findAllByProductCategoryAndResultProperty(productCategory, property)
           .collectList()
           .block();

       return productConsequence != null && !productConsequence.isEmpty();
   }


}
