export enum SamplingPlanMode {
  INTERVAL = 'INTERVAL',
  VALIDATION = 'VALIDATION',
  EVALUATION = 'EVALUATION',
}

export enum LocationType {
  COLLECTION = 'COLLECTION',
  MANUFACTURING = 'MANUFACTURING',
}

export enum SamplingPlanStatus {
  PENDING_APPROVAL = 'PENDING_APPROVAL',
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  PENDING_EDIT = 'PENDING_EDIT',
  PENDING_REVIEW = 'PENDING_REVIEW',
  FINAL_SATISFACTORY = 'FINAL_SATISFACTORY',
  FINAL_UNSATISFACTORY = 'FINAL_UNSATISFACTORY',
}

export enum RequestedSampleStatus {
  IN_PROGRESS = 'IN_PROGRESS',
  MISSING_RESULT = 'MISSING_RESULT',
  UNACCEPTABLE_TEST_RESULT = 'UNACCEPTABLE_TEST_RESULT',
  UNACCEPTABLE_RESIDUAL_WBC_BORDERLINE = 'UNACCEPTABLE_RESIDUAL_WBC_BORDERLINE',
  SUCCESSFUL = 'SUCCESSFUL',
}

export enum RequestedSampleResolution {
  SUCCESS = 'SUCCESS',
  NON_PROCESS_FAILURE = 'NON_PROCESS_FAILURE',
  PROCESS_FAILURE = 'PROCESS_FAILURE',
}

export interface SamplingPlanDto {
  id?: number;
  name?: string;
  description?: string;
  productTypeKey?: string;
  allowableFailures?: number;
  startDate?: string;
  endDate?: string;
  consecutive?: boolean;
  employeeId?: string;
  endReasonKey?: string;
  criteriaLevel1?: string;
  criteriaLevel1Value?: string;
  criteriaLevel2?: string;
  criteriaLevel2Value?: string;
  criteriaLevel3?: string;
  criteriaLevel3Value?: string;
  totalInitialSamples?: number;
  totalAdditionalSamples?: number;
  totalSamplesToCollect?: number;
  pendingSamples?: number;
  processSampleFailures?: number;
  nonProcessSampleFailures?: number;
  successfulSamples?: number;
  totalSamplesCollected?: number;
  mode?: SamplingPlanMode;
  status?: SamplingPlanStatus;
  devices?: SamplingPlanDeviceDto[];
  locationGroupName?: string;
  locations?: SamplingPlanLocationDto[];
  createDate?: string;
  approvalInterpretation?: string;
  approvalDate?: string;
  approvalEmployeeId?: string;

  totalFailedRequestedSamples?: number;
}

export interface SamplingPlanDeviceDto {
  locationId: number;
  serialNumber: string;
  description: string;
}

export interface SamplingPlanLocationDto {
  locationId: number;
  name: string;
}

export interface ProductTypeDto {
  id: number;
  descriptionKey?: string;
  planMode?: SamplingPlanMode;
  criteriaLevel1?: string;
  criteriaLevel1Value?: string;
  criteriaLevel2?: string;
  criteriaLevel2Value?: string;
  criteriaLevel3?: string;
  criteriaLevel3Value?: string;
  locationType?: LocationType;
  orderNumber?: number;
  active?: boolean;
  createDate?: string;
  modificationDate?: string;
  donationTypeKeys?: string[];
}

export interface CreateSamplingPlanDto {
  name: string;
  description?: string;
  productTypeKey: string;
  allowableFailures: number;
  startDate?: string;
  endDate?: string;
  consecutive: boolean;
  criteriaLevel1?: string;
  criteriaLevel1Value?: string;
  criteriaLevel2?: string;
  criteriaLevel2Value?: string;
  criteriaLevel3?: string;
  criteriaLevel3Value?: string;
  mode: SamplingPlanMode;
  devices: SamplingPlanDeviceDto[];
  locationGroupName?: string;
  locations: SamplingPlanLocationDto[];
  tests: CreateSamplingPlanTestDto[];
  editReason?: string;
}

export interface CreateSamplingPlanTestDto {
  locationId: number;
  pqcTestTypeKey?: string;
  initialSamples?: number;
  additionalSamples?: number;
}

export interface ProductTestTypeDto {
  id?: number;
  pqcTestTypeKey: string;
  sampleTaskKey: string;
  conditions?: string;
  interval: string;
  successStatus: string;
  productType: ProductTypeDto[];
  orderNumber: number;
}

export interface CloneSamplingPlanDto {
  name: string;
}

export interface EndSamplingPlanDto {
  reasonKey: string;
}

export interface ApproveSamplingPlanDto {
  approvalInterpretation: string;
  comments?: string;
}

export interface RequestedSampleDto {
  id: number;
  samplingPlanId: number;
  locationId?: number;
  locationName?: string;
  donationId?: number;
  unitNumber?: string;
  pqcTaskKey?: string;
  pqcTestTypeKey?: string;
  pqcTestResult?: string;
  pqcTestOutCome?: string;
  inventoryId?: number;
  inventoryKey?: string;
  productCode?: string;
  status?: string;
  resolution?: string;
  resolutionComments?: string;
  resolutionEmployeeId?: string;
  resolutionDate?: string;
  resolutionDateTimezone?: string;
  reviewComments?: string;
  reviewEmployeeId?: string;
  reviewDate?: string;
  reviewDateTimezone?: string;
  sampleDate?: string;
  sampleDateTimezone?: string;
  expirationDate?: string;
  expirationDateTimezone?: string;
  productDesignation?: string;
  drawDate?: string;
  serialNumber?: string;
  deleteDate?: string;
  deleteDateTimezone?: string;
  createDate?: string;
  createDateTimezone?: string;
  modificationDate?: string;
  modificationDateTimezone?: string;
  crossover?: boolean;
  detailedInfo?: RequestedSampleDetailsDto;
}

export interface RequestedSampleDetailsDto {
  resolution: string;
  pqcTestOutCome: string;
}

export interface ReviewSamplingPlanDto {
  approvalInterpretation: string;
  comments?: string;
}

export interface SamplingPlanTestsDto {
  id: number;
  samplingPlanId: number;
  locationId: number;
  pqcTestTypeKey: string;
  initialSamples: number;
  additionalSamples: number;
  collectedSamples: number;
  totalSamplesToCollect: number;
  countedSamples: number;
}

export interface ResolveSampleFailureDto {
  resolution: string;
  comments?: string;
}

export interface SampleFailureReviewDTO {
  approvalInterpretation: string;
  comments?: string;
}

export interface EvaluationModeDto {
  unitNumber: string;
  donationId: number;
  drawDate: string;
  designation: string;
  productType: string;
  inventoryId: number;
  productCode: string;
  locationId: number;
  sampleDateTime: string;
  sampleTaskKey: string;
  testRequired: string;
  testResult: string;
  testResultInterpretation: string;
  previousCrossover: string;
  samplingPlanNames: string;
}

export interface InventoryEligibilityRequestDto {
  donationId: number;
  inventoryId: number;
  sampleTaskKey: string;
}

export interface CrossoverEligibilityRequestDto extends InventoryEligibilityRequestDto {
  productTypeKey: string;
}

export interface PlanOptionDto {
  id: number;
  name: string;
  description: string;
}

export interface CrossoverEligibilityResponseDto {
  inventoryId: number;
  plans: PlanOptionDto[];
}

export interface SaveCrossoverRequestDto extends CrossoverEligibilityRequestDto {
  samplingPlanIds: Number[];
}
