package com.arcone.biopro.distribution.inventory.infrastructure.config;

import com.arcone.biopro.distribution.inventory.application.mapper.InventoryOutputMapper;
import com.arcone.biopro.distribution.inventory.domain.service.TextConfigService;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapperConfig {

    @Bean("InventoryOutputMapper")
    public InventoryOutputMapper inventoryOutputMapper(TextConfigService textConfigService) {
        InventoryOutputMapper mapper = Mappers.getMapper(InventoryOutputMapper.class);
        mapper.setTextConfigService(textConfigService);
        return mapper;
    }
}
