export interface CreateShipmentRequestDTO {
    customerCode: string;
    shipmentDate: string;
    productType: string;
    cartonTareWeight: number;
    createEmployeeId: string;
    transportationReferenceNumber?: string;
    locationCode: string;
}
