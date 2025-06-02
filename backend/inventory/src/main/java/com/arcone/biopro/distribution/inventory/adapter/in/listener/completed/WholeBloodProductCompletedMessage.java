package com.arcone.biopro.distribution.inventory.adapter.in.listener.completed;

import com.arcone.biopro.distribution.inventory.adapter.in.listener.common.Volume;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Schema(
    name = "WholeBloodProductCompleted",
    title = "WholeBloodProductCompleted",
    description = "Message for completed WholeBloodProductCompleted process"
)
@Builder
public class WholeBloodProductCompletedMessage extends ProductCompletedMessage {
    public WholeBloodProductCompletedMessage() {
        super();
    }
}
