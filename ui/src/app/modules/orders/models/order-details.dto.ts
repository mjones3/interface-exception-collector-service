export interface OrderDetailsDto {
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
    modificationDate: Date;
    deleteDate: Date;
    orderItems: OrderItemDetailsDto[];
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
}
