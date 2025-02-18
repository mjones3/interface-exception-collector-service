package com.arcone.biopro.distribution.inventory.adapter.in.listener.recovered;

import java.time.ZonedDateTime;

public record ProductRecoveredMessage(String unitNumber,
                                      String productCode,
                                      String performedBy,
                                      ZonedDateTime createDate) {}
