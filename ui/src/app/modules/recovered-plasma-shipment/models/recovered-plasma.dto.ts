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
    scheduleDate: string;
    productType: string;
    cartonTareWeight: number;
    createEmployeeId: string;
    transportationReferenceNumber?: string;
    locationCode: string;
}
export interface CreateShipmentResponseDTO {
    id: number;
    locationCode: string;
    productType: string;
    createEmployeeId: string;
    shipmentNumber: string;
    status: string;
    closedEmployeeId: string;
    transportationReferenceNumber: string;
    scheduleDate: string;
    shipmentDate: string;
    cartonTareWeight: number;
    createDate: string;
    modificationDate;
    customerCode: string;
    customerName: string;
}
