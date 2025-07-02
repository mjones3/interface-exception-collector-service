package com.arcone.biopro.distribution.order.domain.model.vo;

import com.arcone.biopro.distribution.order.domain.model.Validatable;
import com.arcone.biopro.distribution.order.domain.repository.LocationRepository;
import com.arcone.biopro.distribution.order.infrastructure.controller.error.DataNotFoundException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;


@Getter
@EqualsAndHashCode
@ToString
@Slf4j
public class OrderLocation implements Validatable {

    private String code;
    private String name;
    private LocationRepository locationRepository;

    public OrderLocation(String code,LocationRepository locationRepository) {
        this.code = code;
        this.locationRepository = locationRepository;
        checkValid();
    }

    @Override
    public void checkValid() {

        if (this.code == null || this.code.isBlank()) {
            throw new IllegalArgumentException("code cannot be null or blank");
        }

        try{
            var location = locationRepository.findOneByCode(code).block();
            if(location == null) {
                throw new IllegalArgumentException("Location not found for code: " + this.code);
            }
            this.name = location.getName();
        }catch (DataNotFoundException ex){
            log.error("Could not find customer with code {}", code, ex);
            throw new IllegalArgumentException("Location not found for code: " + this.code);
        }

        if (this.name == null || this.name.isBlank()) {
            throw new IllegalArgumentException("name cannot be null or blank");
        }
    }
}
