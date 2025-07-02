package com.arcone.biopro.distribution.irradiation.application.mapper;

import com.arcone.biopro.distribution.irradiation.application.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class VolumeGetInventoryBYUnitNumberAndProductInputMapperTest {

    private static final String TYPE = "volume";
    private static final int VALUE = 50;
    private static final String UNIT = "MILLILITERS";

    private VolumeInputMapper mapper;

    @BeforeEach
    public void setup() throws IOException {
        MockitoAnnotations.openMocks(this);
        mapper = Mappers.getMapper(VolumeInputMapper.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new Jdk8Module());
    }

    @DisplayName("should convert volumes to domain")
    @Test
    public void shouldConvertVolumesToDomain(){
        var volumeInputs = List.of(new VolumeInput(TYPE,VALUE, UNIT));
        var outPut = mapper.toDomain(volumeInputs);
        assertEquals(volumeInputs.getFirst().value(), outPut.getFirst().getValue());
        assertEquals(volumeInputs.getFirst().type(), outPut.getFirst().getType());
        assertEquals(volumeInputs.getFirst().unit(), outPut.getFirst().getUnit());
    }

}
