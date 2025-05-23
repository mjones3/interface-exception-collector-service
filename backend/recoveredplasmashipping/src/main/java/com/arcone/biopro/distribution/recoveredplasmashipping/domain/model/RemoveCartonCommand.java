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
    private String employeeId;

    public RemoveCartonCommand(Long cartonId, String employeeId) {
        this.cartonId = cartonId;
        this.employeeId = employeeId;
        checkValid();
    }

    @Override
    public void checkValid() {
        if (cartonId == null) {
            throw new IllegalArgumentException("Carton id is required");
        }

        if (employeeId == null || employeeId.isBlank()) {
            throw new IllegalArgumentException("Employee id is required");
        }
    }
}
