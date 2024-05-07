export interface LookbackTriggerCriteriaDto {
  id: number;
  testGroupId: number;
  testTypeId: number;
  result: string;
  excludeFirstTimeDonor: boolean;
  excludeDonationsWithSampleDonor: boolean;
  excludeDonorAutologousDonation: boolean;
  createDate: Date;
  modificationDate: Date;
}
