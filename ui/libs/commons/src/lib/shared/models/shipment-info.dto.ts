export interface ShipmentInfoDto {
  id: number;
  orderNumber: number;
  priority: string;
  status: string;
  createDate: string;
  shippingCustomerCode?: number;
  locationCode?: number;
  deliveryType?: string;
  shippingMethod: string;
  productCategory?: string;
  shippingDate: string;
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
  items?: ShipmentInfoItemDto[];
}

export interface ShipmentInfoItemDto {
  id: number;
  shipmentId?: number;
  productFamily: string;
  bloodType?: string;
  quantity: number;
  comments?: string;
  shortDateProducts?: ShortDateProductDto[];
  shipmentItemPacked?: FilledProductInfoDto[];
}

export interface ShortDateProductDto {
  id: number;
  shipmentItemId: number;
  unitNumber?: string;
  productCode?: string;
  storageLocation?: string;
  comments?: string;
  createDate?: string;
  modificationDate?: string;
}

export interface FilledProductInfoDto {
  shipmentId: number;
  unitNumber: string;
  productCode?: string;
  productDescription?: string;
  visualInspection?: string;
  collectionDate?: string;
  productFamily?: string;
  expirationDate?: string;
  aboRh?: string;
  storageLocation?: string;
  locationCode?: number;
  createDate?: string;
  modificationDate?: string;
}

export interface VerifyProduct {
  shipmentItemId: number;
  unitNumber: string;
  productCode: string;
  locationCode: number;
  employeeId: string;
  visualInspection: string;
}

export interface VerifyFilledProduct {
  unitNumber: string;
  productCode: string;
  visualInspection: string;
}
