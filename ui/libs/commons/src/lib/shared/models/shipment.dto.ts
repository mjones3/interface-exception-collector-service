import { EmployeeDto } from './employee.dto';

export interface ShipmentDto {
  id?: number;
  orderId: number;
  customerId?: number;
  locationId?: number;
  locationIdTo?: number;
  deliveryType: string;
  shipmentMethod: string;
  readonly employeeId?: string;
  employee?: EmployeeDto;
  statusKey: string;
  state: string;
  postalCode: string;
  country: string;
  countryCode: string;
  city: string;
  district?: string;
  addressLine1: string;
  addressLine2?: string;
  createDate?: string;
  modificationDate?: string;
  deleteDate?: string;
  shipmentItems: ShipmentItemDto[];
}

export interface ShipmentItemDto {
  id?: number;
  shipmentId?: number;
  inventoryId?: number;
  createDate?: string;
  modificationDate?: string;
}

export interface LocationInventoryHistoryDto {
  id?: number;
  inventoryId: number;
  type: 'RETURNS' | 'EXTERNAL_TRANSFERS' | 'IMPORTS' | 'SHIPMENT' | 'INTERNAL';
  customerIdFrom?: number;
  customerIdTo?: number;
  locationId: number;
  locationIdTo: number;
  employeeId?: string;
  createDate?: string;
  modificationDate?: string;
  deleteDate?: string;
}

//TRANSIT TIME
export interface CurrentTimeDto {
  currentDateTime: string;
  currentDate: string;
  currentTime: string;
  hours: string;
  minutes: string;
}

export interface TransitTimeResponseDto {
  totalTransitTime: string;
  totalTransitHours: number;
  responseStatusKey: string;
  responseStatusType: string;
}

export interface TransitTimeRequestDto {
  productCategory: string;
  transitStartDate: string;
  transitStartTime: string;
  transitStartTimeZone: string;
  transitEndDate: string;
  transitEndTime: string;
  transitEndTimeZone: string;
}

//RETURNS
export interface ReturnsDto {
  id?: number;
  employeeId?: string;
  returnNumber?: string;
  returnReasonKey?: string;
  productCategory?: string;
  comments?: string; //maxLength: 1000
  transitStartDateTime?: string;
  transitEndDateTime?: string;
  transitTimeZone?: string;
  totalTransitTime?: string;
  transitTimeResultKey?: string;
  shipmentInspectKey?: string;
  temperature: string; //maxLength: 50
  locationId: number;
  deleteDate?: string;
  createDate?: string;
  modificationDate?: string;
  returnsItems?: ReturnsItemDto[];
}

export interface ReturnsItemDto {
  returnsItemId?: number;
  returnsId?: number;
  inventoryId?: number;
  unitNumber?: string;
  productCode?: string;
  productConsequenceKey?: string;
  createDate?: string;
  modificateDate?: string;
  deleteDate?: string;
  returnsItemConsequences?: ReturnsItemConsequenceDto[];
}

export interface ReturnsItemConsequenceDto {
  id?: number;
  returnsItem?: number;
  itemConsequenceType: string;
  itemConsequenceReasonKey: string;
}
