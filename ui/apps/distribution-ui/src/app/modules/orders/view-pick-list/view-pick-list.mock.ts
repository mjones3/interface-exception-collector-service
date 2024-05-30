export enum BloodType {
  AP,
  OP,
  ON ,
  AN,
}

export enum ShipmentStatus {
  OPEN,
  CANCELLED,
  CLOSED,
}

export enum ShipmentPriority {
  ASAP,
  ROUTINE,
}

export type ShipmentItemShortDateProductResponseDTO = {
  id: number;
  shipmentItemId: number;
  unitNumber: string;
  productCode: string;
  storageLocation: string;
  comments: string;
  createDate: string;
  modificationDate: string;
}

export type ShipmentItemResponseDTO = {
  id: number;
  shipmentId: number;
  productFamily: string;
  bloodType: keyof typeof BloodType;
  quantity: number;
  comments: string;
  shortDateProducts: ShipmentItemShortDateProductResponseDTO[];
}

export type ShipmentDetailResponseDTO = {
  id: number;
  orderNumber: number;
  priority: keyof typeof ShipmentPriority;
  status: keyof typeof ShipmentStatus;
  createDate: string;
  shippingCustomerCode: number;
  locationCode: number;
  deliveryType: string;
  shippingMethod: string;
  productCategory: string;
  shippingDate: string;
  shippingCustomerName: string;
  customerPhoneNumber: string;
  customerAddressState: string;
  customerAddressPostalCode: string;
  customerAddressCountry: string;
  customerAddressCountryCode: string;
  customerAddressCity: string;
  customerAddressDistrict: string;
  customerAddressAddressLine1: string;
  customerAddressAddressLine2: string;
  items: ShipmentItemResponseDTO[];
}

export type DeepPartial<T> = T extends object ? {
  [P in keyof T]?: DeepPartial<T[P]>
} : T;

// FIXME Remove this mock after integration with API
export const VIEW_PICK_LIST_MOCK: DeepPartial<ShipmentDetailResponseDTO> = {
  id: 1,
  orderNumber: 1,
  priority: "ASAP",
  status: "OPEN",
  shippingCustomerCode: 1,
  locationCode: 1,
  deliveryType: "TEST",
  shippingMethod: "TEST",
  productCategory: "Frozen",
  shippingDate: "2024-01-01",
  shippingCustomerName: "Testing Customer",
  customerPhoneNumber: "956532654",
  customerAddressState: "FL",
  customerAddressPostalCode: "8175689",
  customerAddressCountry: "US",
  customerAddressCountryCode: "1",
  customerAddressCity: "Orlando",
  customerAddressDistrict: "District",
  customerAddressAddressLine1: "Address line 1",
  customerAddressAddressLine2: "Address Line 2",
  items: [
    {
      productFamily: "Transfusable Plasma",
      bloodType: "AP",
      quantity: 10,
      comments: "Praesent enim felis, venenatis nec urna nec, facilisis pellentesque libero.",
      shortDateProducts: [
        {
          unitNumber: "W036810946277",
          productCode: "E9747D1",
          storageLocation: "FREEZER 1, RACK 1, SHELF 1"
        },
        {
          unitNumber: "W036810946279",
          productCode: "E9747D2",
          storageLocation: "FREEZER 1, RACK 1, SHELF 1"
        }
      ]
    },
    {
      productFamily: "Transfusable Plasma",
      bloodType: "AN",
      quantity: 5,
      comments: "Lorem ipsum dolor sit amet, consectetur adipiscing elit."
    },
    {
      productFamily: "Transfusable Plasma",
      bloodType: "OP",
      quantity: 8,
      comments: "Proin aliquet vitae sem nec iaculis."
    }
  ]
};
