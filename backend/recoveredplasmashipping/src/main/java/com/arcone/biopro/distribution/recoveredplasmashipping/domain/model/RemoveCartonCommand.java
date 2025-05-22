package com.arcone.biopro.distribution.recoveredplasmashipping.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
public class RemoveCartonCommand implements Validatable {

    private Long cartonId;
    private String employeeI;

    public RemoveCartonCommand(Long cartonId, String employeeI) {
        this.cartonId = cartonId;
        this.employeeI = employeeI;
        checkValid();
    }

    @Override
    public void checkValid() {
        if (cartonId == null) {
            throw new IllegalArgumentException("Carton id is required");
        }

        if (employeeI == null || employeeI.isBlank()) {
            throw new IllegalArgumentException("Employee id is required");
        }
    }
}
