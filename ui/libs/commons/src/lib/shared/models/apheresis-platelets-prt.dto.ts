export interface PrtIlluminatorLoadDto {
  unitNumber?: string;
  localTimeZone?: string;
  facilityId?: number;
  ruleName?: string;
  manufacturingId?: string;
  childInventoryId?: number;
  inventoryIDList?: any[];
  levelOneCode?: string;
}

export interface PrtIlluminatorScanUnitDto {
  unitNumber?: string;
  localTimeZone?: string;
  facilityId?: number;
  ruleName?: string;
  manufacturingId?: string;
}

export interface PrtIlluminatorBagLettersDto {
  unitNumber?: string;
  localTimeZone?: string;
  facilityId?: number;
  ruleName?: string;
  manufacturingId?: string;
  childInventoryId?: number;
}

export interface PrtIlluminatorSubmitDto {
  unitNumber?: string;
  localTimeZone?: string;
  facilityId?: number;
  ruleName?: string;
  manufacturingId?: string;
  inventoryIDList?: any[];
  levelOneCode?: string;
}
