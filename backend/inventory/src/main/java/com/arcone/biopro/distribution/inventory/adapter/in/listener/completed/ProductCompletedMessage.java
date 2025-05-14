package com.arcone.biopro.distribution.inventory.adapter.in.listener.completed;

import com.arcone.biopro.distribution.inventory.adapter.in.listener.common.Volume;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;

public record ProductCompletedMessage(String unitNumber,
                                      String productCode,
                                      Volume volume,
                                      Volume anticoagulantVolume,
                                      AboRhType aboRh) {}
