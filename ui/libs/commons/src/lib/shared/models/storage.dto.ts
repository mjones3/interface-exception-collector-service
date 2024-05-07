export interface StorageDto {
  id?: number;
  employeeId?: string;
  facilityId?: number;
  inventoryId?: number;
  processTypeId?: number;
  levelOneCode?: string;
  levelTwoCode?: string;
  levelThreeCode?: string;
  descriptionKey: string;
  properties?: { [key: string]: object };
  orderNumber?: number;
  modificationDate?: string;
  createDate?: string;
  processIndex?: string;
}

export interface StorageProcessTypeDto {
  id?: number;
  descriptionKey: string;
  active?: boolean;
  orderNumber: number;
  createDate?: string;
  modificationDate?: string;
  level1Label?: string;
}

export interface StorageConfigurationDto {
  id?: number;
  storageProcessTypeId: number;
  storageProcessTypeDescriptionKey?: string;
  level1TypeId: number;
  level1Label: string;
  level2TypeId?: number;
  level2Label?: string;
  level3TypeId?: number;
  level3Label?: string;
  active?: boolean;
  modificationDate?: string;
  createDate?: string;
  status?: string;
}
