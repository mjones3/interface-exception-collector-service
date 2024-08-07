package com.arcone.biopro.distribution.inventory.adapter.in.listener;


public record EventMessage<T>(String eventType, String eventVersion, T payload) {

}
