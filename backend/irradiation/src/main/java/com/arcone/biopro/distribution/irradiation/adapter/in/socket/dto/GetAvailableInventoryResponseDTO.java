package com.arcone.biopro.distribution.irradiation.adapter.in.socket.dto;

import java.io.Serializable;
import java.util.List;

public record GetAvailableInventoryResponseDTO(String location, List<Inventory> inventories) implements Serializable {
}
