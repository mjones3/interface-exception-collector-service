package com.arcone.biopro.distribution.shippingservice.infrastructure.service;

import com.arcone.biopro.distribution.shippingservice.infrastructure.service.dto.FacilityDTO;
import com.arcone.biopro.distribution.shippingservice.infrastructure.service.dto.FacilityMockData;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FacilityServiceMock {

    private final ObjectMapper objectMapper;
    private List<FacilityDTO> facilityDTOList;

    public Mono<FacilityDTO> getFacilityId(Integer facilityId) {
        if(facilityDTOList == null){
            initFacilityMockList();
        }

        return facilityDTOList.stream()
            .filter(facility -> Objects.equals(facility.id(), facilityId))
            .findAny()
            .map(Mono::just)
            .orElseGet(() -> Mono.error(new RuntimeException("facility-not-found.error")));
    }

    private void initFacilityMockList() {
        facilityDTOList = new ArrayList<>();
        try {
            var fileInputStream = new ClassPathResource("mock/facility/facility-mock-data.json").getInputStream();
            var mockData = objectMapper.readValue(fileInputStream, FacilityMockData.class);
            facilityDTOList = mockData.data();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

