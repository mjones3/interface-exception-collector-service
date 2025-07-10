package com.arcone.biopro.distribution.irradiation.unit.adapter;

import com.arcone.biopro.distribution.irradiation.adapter.in.web.dto.ConfigurationResponseDTO;
import com.arcone.biopro.distribution.irradiation.adapter.in.web.mapper.ConfigurationDTOMapper;
import com.arcone.biopro.distribution.irradiation.adapter.irradiation.IrradiationResource;
import com.arcone.biopro.distribution.irradiation.domain.model.Configuration;
import com.arcone.biopro.distribution.irradiation.domain.repository.ConfigurationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IrradiationResourceTest {

    @Mock
    private ConfigurationService configurationService;

    @Mock
    private ConfigurationDTOMapper configurationDTOMapper;

    @InjectMocks
    private IrradiationResource irradiationResource;



    @Test
    @DisplayName("Should return configuration response DTOs")
    void readConfiguration_Success() {
        Configuration config = Configuration.builder().build();
        ConfigurationResponseDTO responseDTO = ConfigurationResponseDTO.builder().build();

        when(configurationService.readConfiguration(anyList()))
                .thenReturn(Flux.just(config));
        when(configurationDTOMapper.toResponseDTO(any(Configuration.class)))
                .thenReturn(responseDTO);

        Flux<ConfigurationResponseDTO> result = irradiationResource.readConfiguration(List.of("key1"));

        StepVerifier.create(result)
                .expectNext(responseDTO)
                .verifyComplete();
    }
}
