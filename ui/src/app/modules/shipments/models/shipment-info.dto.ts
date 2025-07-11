export enum ShipmentPriority {
    ASAP,
    ROUTINE,
}

export enum ShipmentStatus {
    OPEN,
    CANCELLED,
    CLOSED,
    COMPLETED,
}

export interface ShipmentResponseDTO {
    id?: number;
    orderNumber?: number;
    priority?: keyof typeof ShipmentPriority;
    status?: keyof typeof ShipmentStatus;
    createDate?: string;
}

export interface ShipmentDetailResponseDTO {
    id: number;
    orderNumber: number;
    externalId?: string;
    priority?: string;
    status: string;
    createDate: string;
    shippingCustomerCode?: string;
    locationCode?: number;
    deliveryType?: string;
    shippingMethod: string;
    productCategory?: string;
    shippingDate?: string;
    shippingCustomerName?: string;
    customerPhoneNumber?: string;
    customerAddressState?: string;
    customerAddressPostalCode: string;
    customerAddressCountry?: string;
    customerAddressCountryCode: string;
    customerAddressCity?: string;
    customerAddressDistrict?: string;
    customerAddressAddressLine1?: string;
    customerAddressAddressLine2?: string;
    completeDate?: string;
    completedByEmployeeId?: string;
    comments?: string;
    items?: ShipmentItemResponseDTO[];
    checkDigitActive?: boolean;
    visualInspectionActive?: boolean;
    secondVerificationActive?: boolean;
    labelStatus?:string;
    shipmentType?:string;
    quarantinedProducts?:boolean;
}

export interface ShipmentItemResponseDTO {
    id?: string;
    shipmentId?: number;
    productFamily?: string;
    bloodType?: string;
    quantity?: number;
    comments?: string;
    shortDateProducts?: ShipmentItemShortDateProductResponseDTO[];
    packedItems?: ShipmentItemPackedDTO[];
}

export interface ShipmentItemShortDateProductResponseDTO {
    id: number;
    shipmentItemId: number;
    unitNumber?: string;
    productCode?: string;
    storageLocation?: string;
    comments?: string;
    createDate?: string;
    modificationDate?: string;
}

export enum VisualInspection {
    SATISFACTORY,
    UNSATISFACTORY,
    DISABLED,
}

export enum SecondVerification {
    PENDING,
    COMPLETED,
    DISABLED,
}

export interface ShipmentItemPackedDTO {
    id?: number;
    shipmentItemId?: number;
    inventoryId?: number;
    unitNumber?: string;
    productCode?: string;
    aboRh?: string;
    productDescription?: string;
    productFamily?: string;
    expirationDate?: string;
    collectionDate?: string;
    packedByEmployeeId?: string;
    visualInspection?: keyof typeof VisualInspection;
    secondVerification?: keyof typeof SecondVerification;
    verifiedByEmployeeId?: string;
    verifiedDate?: string;
    ineligibleStatus?: string;
    ineligibleAction?: string;
    ineligibleReason?: string;
    ineligibleMessage?: string;
    ineligibleDetails?: string[];
    productStatus?:string;
}

export interface VerifyProductDTO {
    shipmentItemId: number;
    unitNumber: string;
    productCode: string;
    locationCode?: string;
    employeeId?: string;
    visualInspection?: string;
}

export interface VerifyFilledProductDto {
    unitNumber: string;
    productCode: string;
    visualInspection?: string;
}

export interface ShipmentCompleteInfoDto {
    completedByEmployeeName?: string;
    completeDate?: string;
    quantity?: number;
}
