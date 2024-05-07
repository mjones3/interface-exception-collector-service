export interface PositiveBactFollowUpDetailDto {
  pqcTestType: string;
  pqcTestResult: string;
  gramStain?: string;
  organismId?: string;
  systemFinalInterpretation: string;
  appliedFinalInterpretation?: string;
  comment?: string;
  closeComment?: string;
  initialBactProduct?: BactProductDto;
  associatedBactProducts: BactProductDto[];
  closeDate?: Date;
}

export interface BactProductDto {
  id: number;
  unitNumber: string;
  productDescription: string;
  isQuarantine: boolean;
  quarantineStatus?: string;
  inventoryId?: number;
  inventoryStatus: string;
  sampleStatus: string;
  sampleStatusComment?: string;
  bactTestResults: BactTestResultDto[];
  bactProdResults?: BactResultDto[];
  moreInfo?: boolean;
}

export interface BactTestResultDto {
  testType: string;
  testResult: string;
  testResultClass?: string;
  testDate: string;
}

export interface BactResultDto {
  productShortDescription: string;
  quarantineStatus: string;
  inventoryStatus: string;
}
