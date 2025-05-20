package com.arcone.biopro.distribution.recoveredplasmashipping.domain.model;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
public class RepackCartonCommand implements Validatable {

    private Long cartonId;
    private String employeeId;
    private String locationCode;
    private String reasonComments;
    private static final int MAX_COMMENTS = 250;

    public RepackCartonCommand(Long cartonId, String employeeId, String locationCode, String reasonComments) {
        this.cartonId = cartonId;
        this.employeeId = employeeId;
        this.locationCode = locationCode;
        this.reasonComments = reasonComments;

        checkValid();
    }

    @Override
    public void checkValid() {

        if(cartonId == null ){
            throw new IllegalArgumentException("Carton ID is required");
        }

        if(employeeId == null || employeeId.isBlank()){
            throw new IllegalArgumentException("Employee ID is required");
        }

        if(locationCode == null || locationCode.isBlank()){
            throw new IllegalArgumentException("Location code is required");
        }

        if(reasonComments == null || reasonComments.isBlank()){
            throw new IllegalArgumentException("Reason comments is required");
        }

        if(reasonComments.length() > MAX_COMMENTS){
            throw new IllegalArgumentException("Reason comments cannot exceed : "+MAX_COMMENTS);
        }
    }
}
