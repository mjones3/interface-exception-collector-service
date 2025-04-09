package com.arcone.biopro.distribution.recoveredplasmashipping.application.dto;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class UseCaseMessage {

    final UseCaseNotificationType type;
    final String message;
    final Integer code;

    public UseCaseMessage (UseCaseMessageType useCaseMessageType){
        this.type = useCaseMessageType.getType();
        this.message = useCaseMessageType.getMessage();
        this.code = useCaseMessageType.getCode();
    }

    public UseCaseMessage (Integer code , UseCaseNotificationType type , String message){
        this.type = type;
        this.message = message;
        this.code = code;
    }
}
