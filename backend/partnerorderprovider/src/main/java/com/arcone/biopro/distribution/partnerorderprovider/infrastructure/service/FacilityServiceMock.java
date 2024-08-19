package com.arcone.biopro.distribution.partnerorderprovider.infrastructure.service;

import com.arcone.biopro.distribution.partnerorderprovider.infrastructure.service.dto.FacilityDTO;
import com.arcone.biopro.distribution.partnerorderprovider.infrastructure.service.dto.FacilityMockData;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FacilityServiceMock {

    private final ObjectMapper objectMapper;
    private List<FacilityDTO> facilityDTOList;

    public FacilityDTO getFacilityByExternalCode(String externalCode) {
        if(facilityDTOList == null){
            initFacilityMockList();
        }

        return facilityDTOList.stream()
            .filter(facility -> Objects.equals(facility.externalId(), externalCode))
            .findAny()
            .orElseThrow(() -> new RuntimeException("Facility not found."));
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

