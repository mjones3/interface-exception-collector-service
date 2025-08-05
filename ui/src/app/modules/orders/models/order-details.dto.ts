export interface OrderDetailsDTO {
    id: number;
    orderNumber: number;
    externalId: string;
    locationCode: string;
    shipmentType: string;
    shippingMethod: string;
    shippingCustomerName: string;
    shippingCustomerCode: string;
    billingCustomerName: string;
    billingCustomerCode: string;
    desiredShippingDate: Date;
    willCallPickup: boolean;
    phoneNumber: string;
    productCategory: string;
    comments: string;
    status: string;
    priority: string;
    createEmployeeId: string;
    createDate: Date;
    modificationDate: string;
    deleteDate: Date;
    orderItems: OrderItemDetailsDto[];
    totalShipped: number;
    totalRemaining: number;
    totalProducts: number;
    canBeCompleted: boolean;
    completeEmployeeId: string;
    completeDate: string;
    completeComments: string;
    backOrderCreationActive: boolean;
    cancelEmployeeId: string;
    cancelDate: string;
    cancelReason: string;
    modifyEmployeeId?: string;
    modifyReason?: string;
    modifyByProcess?: string;
    displayModificationDetails: boolean;
    quarantinedProducts?:boolean;
    labelStatus?:string;
}

export interface OrderItemDetailsDto {
    id: number;
    orderId: number;
    productFamily: string;
    bloodType: string;
    quantity: number;
    comments: string;
    createDate: Date;
    modificationDate: Date;
    quantityAvailable: number;
    quantityShipped: number;
    quantityRemaining: number;
}
