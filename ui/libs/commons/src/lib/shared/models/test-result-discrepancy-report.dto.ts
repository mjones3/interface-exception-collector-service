export interface TestResultDiscrepancyReportDto {
  unitNumber: string;
  driveId?: string;
  drawDate?: string;
  initialTestResultId: number;
  discoveryDate: string;
  status: string;
  locationId: number;
  activeResult: string;
  quarantineStatus?: string;
  testTypeId: number;
  testTypeDescriptionKey: string;
  locationName: string;
  regionId: number;
  regionName: string;
  initialTestResult: string;
  initialTestResultCreateDate: string;
  initialTestResultCreateEmployeeId: string;
  resolutionDate?: string;
  resolvedEmployeeId?: string;
  resolutionReasonKey?: string;
}
