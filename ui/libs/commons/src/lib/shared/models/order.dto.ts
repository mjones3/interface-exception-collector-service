export interface OrderSummaryDto {
  id?: number;
  orderNumber?: string;
  externalId?: string;
  orderLocationId?: number;
  deliveryType?: string;
  shippingMethod?: string;
  desireShippingDate?: string;
  createDate?: string;
  statusKey?: string;
  priority?: number;
  cancelDate?: string;
  completeDate?: string;
  shippingCustomerName?: string;
  shippingCustomerId?: number;
  shippingCustomerExternalId?: string;
  billingCustomerName?: string;
  billingCustomerId?: number;
  billingCustomerExternalId?: string;
  comments?: string;
}

export type LabelStatus = 'LABELED' | 'UNLABELED';

export interface OrderDto {
  id?: number;
  orderNumber: number;
  externalId: string; //maxLength=50
  shippingCustomerId: number;
  billingCustomerId: number;
  locationId: number;
  deliveryType: string; //maxLength=255
  shipmentType?: string;
  shipToLocation?: string;
  labelStatus?: LabelStatus;
  priority: number;
  shippingMethod: string; //maxLength=255
  productCategoryKey: string;
  desireShippingDate: string;
  comments?: string; //maxLength=1000
  statusKey: string; //maxLength=255
  cancelReasonId?: number;
  cancelDate?: string;
  cancelEmployeeId?: string; //maxLength=50
  reviewDate?: string;
  reviewEmployeeId?: string; //maxLength=50
  downtimeOrder: boolean;
  downtimeComments?: string; //maxLength=1000
  externalOrder: boolean;
  completeReasonId?: number;
  completeDate?: string;
  completeEmployeeId?: string; //maxLength=50
  deleteDate?: string;
  createDate?: string;
  modificationDate?: string;
  orderItems?: OrderItemDto[];
  orderServiceFees: OrderServiceFeeDto[];
  employeeId?: string;
  shippingLocationId?: number;
}

export interface OrderItemDto {
  id?: number;
  orderId?: number;
  productFamily: string; //maxLength=255
  bloodTypeId: number;
  quantity: number;
  comments?: string;
  filled: boolean;
  createDate?: string;
  modificationDate?: string;
  orderItemProductAttributes: OrderItemProductAttributeDto[];
  orderItemAttachments?: OrderItemAttachmentDto[];
  orderItemInventories?: OrderItemInventoryDto[];
}

export interface OrderItemProductAttributeDto {
  id?: number;
  orderItemId?: number;
  productAttributeId: number;
  productAttributeOptions?: ItemProductAttributeOptionDto[];
}

export interface ItemProductAttributeOptionDto {
  id?: number;
  orderItemProductAttributeId?: number;
  attributeOptionValue: string;
}

export interface OrderServiceFeeDto {
  id?: number;
  orderId?: number;
  serviceFee: string; //maxLength=255
  quantity: number;
}

export interface OrderBloodTypeDto {
  id?: number;
  productFamily: string; //maxLength=255
  bloodTypeValue: string; //maxLength=255
  descriptionKey: string; //maxLength=255
  orderNumber: number;
  active: boolean;
  createDate?: string;
  modificationDate?: string;
}

export interface OrderProductAttributeDto {
  id?: number;
  descriptionKey: string; //maxLength=255
  attributeValue: string; //maxLength=255
  propertyKey: string; //maxLength=255
  propertyValue: string; //maxLength=255
  color: string; //maxLength=255
  orderNumber: number;
  createDate?: string;
  modificationDate?: string;
  attributeOptions?: OrderProductAttributeOptionDto[];
}

export interface OrderProductAttributeOptionDto {
  id?: number;
  descriptionKey: string; //maxLength=255
  optionValue: string; //maxLength=255
  orderNumber: number;
  active: boolean;
  createDate?: string;
  modificationDate?: string;
}

export interface OrderProductFamilyDto {
  id?: number;
  familyCategory: string;
  familyType: string;
  descriptionKey: string; //maxLength=255
  familyValue: string; //maxLength=255
  active: boolean;
  createDate?: string;
  modificationDate?: string;
}

export interface OrderItemAttachmentDto {
  id?: number;
  orderItemId: number;
  description: string; //maxLength: 255
  documentId: number;
  createDate?: string;
  modificationDate?: string;
}

export interface OrderItemProductDTO {
  unitNumber: string;
  productCode: string;
  unlicensed: boolean;
  orderItemInventory: OrderItemInventoryDto;
}

export interface OrderItemInventoryDto {
  id?: number;
  order: number;
  orderItem: number;
  inventoryId: number;
  price?: string;
  filled: boolean;
  createDate?: string;
  modificationDate?: string;
  productCode?: string;
  unitNumber?: string;
  validated?: boolean;
}
