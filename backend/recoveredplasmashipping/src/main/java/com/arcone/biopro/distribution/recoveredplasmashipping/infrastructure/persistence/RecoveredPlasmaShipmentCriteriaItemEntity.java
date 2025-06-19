package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@Table("lk_recovered_plasma_shipment_criteria_item")
public class RecoveredPlasmaShipmentCriteriaItemEntity {
    @Id
    @Column("recovered_plasma_shipment_criteria_id")
    private Integer criteriaId;

    @Column("type")
    private String type;

    @Column("value")
    private String value;

    @Column("message")
    private String message;

    @Column("message_type")
    private String messageType;


}

