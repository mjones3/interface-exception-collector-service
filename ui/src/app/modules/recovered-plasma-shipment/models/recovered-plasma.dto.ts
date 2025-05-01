export interface CreateShipmentRequestDTO {
    customerCode: string;
    shipmentDate?: string;
    productType: string;
    cartonTareWeight: number;
    createEmployeeId: string;
    transportationReferenceNumber?: string;
    locationCode: string;
}

export interface CartonDTO {
    id?: number;
    cartonNumber?: string;
    shipmentId?: number;
    cartonSequence?: number;
    createEmployeeId?: string;
    closeEmployeeId?: string;
    createDate?: string;
    modificationDate?: string;
    closeDate?: string;
    status?: string;
    totalProducts?: number;
    totalWeight?: number;
    totalVolume?: number;
    maxNumberOfProducts?: number;
    minNumberOfProducts?: number;
    packedProducts?: CartonPackedItemResponseDTO[];
}

export interface RecoveredPlasmaShipmentResponseDTO {
    id?: number;
    locationCode?: string;
    productType?: string;
    shipmentNumber?: string;
    createEmployeeId?: string;
    status?: string;
    closedEmployeeId?: string;
    closeDate?: string;
    closeEmployeeId?: string;
    transportationReferenceNumber?: string;
    unsuitableUnitReportDocumentStatus?: string;
    scheduleDate?: string;
    shipmentDate?: string;
    cartonTareWeight?: number;
    createDate?: string;
    modificationDate?: string;
    customerCode?: string;
    customerName?: string;
    customerState?: string;
    customerPostalCode?: string;
    customerCountry?: string;
    customerCountryCode?: string;
    customerCity?: string;
    customerDistrict?: string;
    customerAddressLine1?: string;
    customerAddressLine2?: string;
    customerAddressContactName?: string;
    customerAddressPhoneNumber?: string;
    customerAddressDepartmentName?: string;
    totalCartons?: number;
    totalProducts?: number;
    canAddCartons?: boolean;
    cartonList?: CartonDTO[];
}

export interface CartonPackedItemResponseDTO {
    id?: number;
    cartonId?: string;
    unitNumber?: string;
    productCode?: string;
    productDescription?: string;
    productType?: string;
    volume?: number;
    weight?: number;
    packedByEmployeeId?: string;
    aboRh?: string;
    status?: string;
    expirationDate?: string;
    collectionDate?: string;
    createDate?: string;
    modificationDate?: string;
}
