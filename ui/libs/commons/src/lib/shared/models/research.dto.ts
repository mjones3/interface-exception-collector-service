export interface ResearchDto {
  id?: number;
  name: string;
  startDate: string;
  endDate: string;
  restartDate?: string;
  completeDate?: string;
  priority: number;
  frequency: string;
  numberOfProducts: number;
  totalProductsAssigned?: number;
  totalProductsRequired?: number;
  employeeId?: string;
  status?: string;
  bloodTypes?: string[];
  assignedFamilies?: string[];
  flaggedFamilies?: string[];
  products?: string[];
  locations?: number[];
  properties?: { [key: string]: string };
  researchTestResults?: ResearchTestResultDto[];
  createDate?: string;
  deleteDate?: string;
  modificationDate?: string;
}

export interface ResearchAssignedInventoryDTO extends ResearchInventoryDto {
  productCode?: string;
  unitNumber?: string;
  shipDate?: string;
}

export interface ResearchFlaggedInventoryDTO extends ResearchInventoryDto {
  id?: number;
  researchId?: number;
  inventoryId?: number;
  productKey?: string;
}

export interface ResearchInventoryDto {
  id?: number;
  researchId?: number;
  inventoryId?: number;
  productKey?: string;
  createDate?: string;
  deleteDate?: string;
  modificationDate?: string;
}

export interface ResearchTestResultDto {
  id?: number;
  researchId?: number;
  testTypeId: number;
  testTypeKey: string;
  result: string;
  resultKey: string;
  createDate?: string;
  deleteDate?: string;
  modificationDate?: string;
}
