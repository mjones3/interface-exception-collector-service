export interface LookbackTestResultDetailDto {
  id: number;
  dateOfDiscovery: string;
  testGroup: string;
  testType: string;
  result: string;
  generatedLookback: string;
  reviewEmployeeId: string;
}
