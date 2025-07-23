package com.arcone.biopro.distribution.customer.adapter.in.web.listener;

import lombok.*;

import java.time.ZonedDateTime;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class MessageDTO<T> {

    private String eventId;
    private ZonedDateTime occurredOn;
    private String eventVersion;
    private String eventType;
    private T payload;

}
