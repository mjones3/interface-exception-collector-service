package com.arcone.biopro.distribution.inventory.adapter.in.listener.discarded;

import java.time.ZonedDateTime;

public record ProductDiscardedMessage(String unitNumber,
                                      String productCode,
                                      String reasonDescriptionKey,
                                      String comments,
                                      String triggeredBy,
                                      String performedBy,
                                      ZonedDateTime createDate) {}
