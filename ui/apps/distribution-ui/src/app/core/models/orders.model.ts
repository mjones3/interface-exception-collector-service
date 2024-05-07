import {
  OrderBloodTypeDto,
  OrderDto,
  OrderItemAttachmentDto,
  OrderProductAttributeDto,
  OrderProductAttributeOptionDto,
  OrderProductFamilyDto,
  OrderSummaryDto,
} from '@rsa/commons';

export const ORDER_PROCESS_UUID = '96ac8e30-371f-4e9e-9578-3f48c5f0b0de';
export const ORDER_STATUS = 'ORDER_STATUS';
export const ORDER_LABEL_STATUS = 'ORDER_LABEL_STATUS';
export const ORDER_DELIVERY_TYPE = 'ORDER_DELIVERY_TYPE';
export const ORDER_SHIPPING_METHOD = 'ORDER_SHIPPING_METHOD';
export const ORDER_SHIPMENT_TYPE = 'ORDER_SHIPMENT_TYPE';
export const ORDER_SERVICE_FEE = 'ORDER_SERVICE_FEE';
export const ORDER_PRODUCT_CATEGORY = 'ORDER_PRODUCT_CATEGORY';
export const ANTIGEN_TESTED = 'ANTIGEN_TESTED';
export const OPEN_OPTION_VALUE = 'OPEN';
export const CANCELLED_OPTION_VALUE = 'CANCELLED';
export const SHIPPED_OPTION_VALUE = 'SHIPPED';
export const ORDER_BILLING_CUSTOMER = 'BILLING';
export const ORDER_SHIPPING_CUSTOMER = 'SHIPPING';
export const ORDER_ITEM_LOCK_TYPE = 'ORDER_ITEM';
export const CANCEL_REASON_ID = 16;
export const CLOSE_REASON_ID = 11;
export const SHIPPING_METHOD_DEFAULT_VALUE = 'COURIER';
export const DELIVERY_TYPE_DEFAULT_VALUE = 'ROUTINE';
export const ORDER_PRODUCT_CATEGORY_ROOM_TEMPERATURE = 'ORDER_PRODUCT_CATEGORY_ROOM_TEMPERATURE';
export const ORDER_PRODUCT_CATEGORY_REFRIGERATED = 'ORDER_PRODUCT_CATEGORY_REFRIGERATED';
export const ORDER_PRODUCT_CATEGORY_FROZEN = 'ORDER_PRODUCT_CATEGORY_FROZEN';
export const ORDER_OUT_OF_STATE_RULE_NAME = 'rul-0115-internal-transfer-out-of-state';
export const ORDER_LABELED_OPTION = 'LABELED';
export const ORDER_UNLABELED_OPTION = 'UNLABELED';
export const ORDER_LOCATION_TYPE_ID = 5;

export type LabelingProductCategoryType =
  | 'ORDER_PRODUCT_CATEGORY_REFRIGERATED'
  | 'ORDER_PRODUCT_CATEGORY_FROZEN'
  | 'ORDER_PRODUCT_CATEGORY_ROOM_TEMPERATURE';

export enum OrderStatuses {
  OPEN = 'ORDER_STATUS_COLOR_OPEN',
  SHIPPED = 'ORDER_STATUS_COLOR_SHIPPED',
  CANCELLED = 'ORDER_STATUS_COLOR_CANCELLED',
}

export interface OrderSummary extends OrderSummaryDto {
  statusDescriptionKey?: string;
  statusColor?: string;
  deliveryTypeDescriptionKey?: string;
}

export interface OrderFee {
  id?: number;
  serviceFee?: ServiceFee;
  quantity?: number;
}

export interface ServiceFee {
  id?: number;
  name?: string;
}

export interface OrderPriority {
  id?: number;
  name?: string;
}

export interface ShippingMethod {
  id?: number;
  name?: string;
}

export interface LabelProdCategory {
  id?: number;
  name?: string;
}

export type CustomerSearchCriteriaType = 'customer-name' | 'customer-id';

export interface Order {
  id?: number;
  externalOrderId?: string;
  orderNumber: string;
  shipToCustomerId: number;
  billToCustomerId: number;
  customerSearchCriteria?: CustomerSearchCriteriaType;
  createDate: string;
  shipDate: string;
  priority: OrderPriority;
  shippingMethod: ShippingMethod;
  status: string;
  labelingProdCategory?: LabelProdCategory;
  comments?: string;
  addedFees?: OrderFee[];
}

export interface OrderModel extends OrderDto {
  shipToCustomer: string;
  billToCustomer: string;
}

export interface FilledProduct {
  unitNumber: string;
  productCode: string;
  priceOverride?: number;
  unlicensedProduct?: boolean;
  validated?: boolean;
}

export interface OrderProduct {
  id?: number;
  quantity: number;
  bloodType: OrderBloodTypeDto;
  bloodTypeAndQuantity?: BloodTypeAndQuantity[];
  productAttributes?: OrderProductAttributeDto[];
  productFamily: OrderProductFamilyDto;
  productComment?: string;
  antigensTested?: OrderProductAttributeOptionDto[];
  quantityFilledProducts?: number;
  attachments?: OrderItemAttachmentDto[];
  filledProducts?: FilledProduct[]; //TODO: Remove once validate order gets integrated
  rowIndex?: number; //To be able to expand products that were not added to the database yet
}

export interface ShippedProduct extends OrderProduct {
  shipmentDate: string | Date;
  employee: string;
}

export interface BloodTypeAndQuantity {
  quantity: number;
  bloodType: OrderBloodTypeDto;
}

export enum ShipmentType {
  CUSTOMER = 'CUSTOMER',
  INTERNAL = 'INTERNAL',
  SPECIALTY_LAB = 'SPECIALTY_LAB',
}
