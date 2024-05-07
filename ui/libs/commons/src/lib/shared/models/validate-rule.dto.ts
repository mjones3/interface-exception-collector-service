export interface ValidateRuleDto {
  unitNumber?: string;
  unitNumbers?: string[];
  productCode?: string;
  isbtProductCode?: string;
  ruleName?: string;
  facilityId?: number;
  processDeviceTypeId?: number;
  centrifugeId?: number;
  inventoryId?: number;
  reasonId?: number;
  bagLetter?: string;
  calculatedVolume?: number;
  splitVolumeRangeConfirmation?: boolean;
  barcode?: String;
  typeId?: number;
  typeIds?: number[];
  initialFreezerConfiguration?: string;
  centrifugeTypeId?: number;
  downtimeDate?: string;

  //Product Data Entry
  donationTypeKey?: string;
  pathway?: string;
  parentVolume?: number;
  productVolume?: number;
  yield?: number;
  anticoagulantVolume?: number;
  createProduct?: string;
  localTimeZone?: string;

  //Edit Volume
  volume?: number;
  comment?: string;

  //Plasma Conversion
  type?: string;

  [key: string]: any;
}
