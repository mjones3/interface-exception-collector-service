export interface LookbackDto {
  id: number;
  donorId: number;
  donationId: number;
  unitNumber: string;
  testResultId: number;
  testGroupId: number;
  result: string;
  resultDiscoveryDate?: string;
  currentStatusKey: string;
  clientTimezone?: string;
  createDate: string;
  modificationDate: string;
}
