import { InventoryDto } from './inventory.dto';

export interface TransferReceiptDto {
  id: number;
  employeeId: string;
  orderNumber: number;
  orderId: number;
  comments: string;
  transitStartDateTime: string;
  transitEndDateTime: string;
  transitTimeZone: string;
  totalTransitTime: string;
  transitTimeResultKey: string;
  shipmentInspectKey: string;
  temperature: string;
  locationId: number;
  createDate: string;
  createDateTimezone: string;
  modificationDate: string;
  modificationDateTimezone: string;
  transferReceiptItems: Partial<{
    id: number;
    transferReceiptId: number;
    inventoryId: number;
    unitNumber: string;
    productCode: string;
    createDate: string;
    modificationDate: string;
    transferReceiptItemConsequences?: Partial<{
      id: number;
      transferReceiptItemId: number;
      itemConsequenceType: string;
      itemConsequenceReasonKey: string;
    }>[];
  }>[];
}

export interface ProductSelectionItemDto extends InventoryDto {
  currentFacilityId: number;
  createDate: Date;
  processIndex: string;
  id: number;
  icon: string;
  status: string;
  isQuarantine: boolean;
  quarantine?: boolean;
  descriptionKey: string;
  facilityId: number;
  donationId: number;
  unitNumber: string;
  discardDate: Date;
  properties: {
    [key: string]: string;
  };
  modificationDate: Date;
  notifications: string;
  parentId: number;
  productCode: string;
  deleteDate: Date;
}
