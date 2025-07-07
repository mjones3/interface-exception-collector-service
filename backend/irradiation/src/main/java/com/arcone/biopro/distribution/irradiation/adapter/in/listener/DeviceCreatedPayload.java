package com.arcone.biopro.distribution.irradiation.adapter.in.listener;

public record DeviceCreatedPayload(
    String id,
    String location,
    String deviceCategory,
    String status
) {}