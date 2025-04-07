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

export interface CreateShipmentRequestDTO {
    customerCode: string;
    shipmentDate: string;
    productType: string;
    cartonTareWeight: number;
    createEmployeeId: string;
    transportationReferenceNumber?: string;
    locationCode: string;
}
