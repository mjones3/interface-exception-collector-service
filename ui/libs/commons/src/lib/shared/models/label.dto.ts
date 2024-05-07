export interface LabelDto {
  id?: number;
  inventoryId: number;
  templateType?: string;
  collectionTypeCode?: string;
  licenceNumber?: string;
  registrationNumber?: string;
  bloodType?: string;
  productCode?: string;
  unitNumber?: string;
  expirationDate?: string;
  collectionDate?: string;
  validationDate?: string;
  voidDate?: string;
  emergencyRelease?: boolean;
  deleteDate?: string;
  createDate?: string;
  modificationDate?: string;
  expirationDateDisplay?: string;
  comment?: string;
  properties?: Map<string, any>;
  labelContentDTO?: LabelContentDto;
  labelCount?: number;
  updateToUnlicensed?: boolean;
}

export interface LabelContentDto {
  id?: number;
  labelId: number;
  content: string;
  deleteDate?: string;
  createDate?: string;
  modificationDate?: string;
}
