package com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Validatable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
public class RecoveredPlasmaShipmentCriteriaItem implements Validatable {

    private final String type;
    private final String value;
    private final String message;
    private final String messageType;


    public RecoveredPlasmaShipmentCriteriaItem(String type, String value, String message, String messageType) {
        this.type = type;
        this.value = value;
        this.message = message;
        this.messageType = messageType;
        checkValid();
    }

    @Override
    public void checkValid() {
        if(type == null || type.isBlank()){
            throw new IllegalArgumentException("Type is required");
        }

        if(value == null || value.isBlank()){
            throw new IllegalArgumentException("Value is required");
        }

        if(message == null || message.isBlank()){
            throw new IllegalArgumentException("Message is required");
        }

        if(messageType == null || messageType.isBlank()){
            throw new IllegalArgumentException("Message type is required");
        }



    }
}
