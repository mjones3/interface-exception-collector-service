export interface CustomerDto {
  id?: number;
  externalId: string;
  name: string;
  code?: string;
  phoneNumber?: string;
  customerAddresses: CustomerAddressDto[];
  isbtCodeList: CustomerIsbtCodeDto[];
  createDate?: string;
  modificationDate?: string;
  deleteDate?: string;
}

export interface CustomerAddressDto {
  id?: number;
  customerId: number;
  addressType: string;
  city: string;
  contactName: string;
  state: string;
  postalCode: string;
  country: string;
  countryCode: string;
  district: string;
  addressLine1: string;
  addressLine2: string;
}

export interface CustomerIsbtCodeDto {
  id?: number;
  customerId?: number;
  code: string;
  orderNumber: number;
}
