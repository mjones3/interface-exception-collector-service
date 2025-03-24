export interface ShipmentFilterDTO {
    shipmentNumber?: string;
    shipmentStatus?: string[];
    customers?: string[];
    productFamily?: string[];
    shipmentDateRange?: ShipmentFilterDateRangeDTO;
}

export interface ShipmentFilterDateRangeDTO {
    start: string;
    end: string;
}
