package com.arcone.biopro.distribution.shippingservice.infrastructure.service;

import com.arcone.biopro.distribution.shippingservice.infrastructure.controller.dto.InventoryMockData;
import com.arcone.biopro.distribution.shippingservice.infrastructure.service.dto.FacilityDTO;
import com.arcone.biopro.distribution.shippingservice.infrastructure.service.dto.FacilityMockData;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FacilityServiceMock {

    private final ObjectMapper objectMapper;
    private List<FacilityDTO> facilityDTOList;

    public Mono<FacilityDTO> getFacilityId(Integer facilityId) {
        if(facilityDTOList == null){
            initFacilityMockList();
        }

        var facility = facilityDTOList.stream().filter(facilityDTO -> facilityDTO.id().equals(facilityId)).findAny();
        if(facility.isPresent()){
            return Mono.just(facility.get());
        }else {
            return Mono.error(new RuntimeException("facility-not-found.error"));
        }
    }

    private void initFacilityMockList(){
        facilityDTOList = new ArrayList<>();
        try {

            var file = ResourceUtils.getFile("classpath:mock/facility/facility-mock-data.json");
            var mockData = objectMapper.readValue(file, FacilityMockData.class);
            facilityDTOList = mockData.data();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
