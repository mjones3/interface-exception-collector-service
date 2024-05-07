export interface DhqDeferredDonorReportDTO {
  id: number;
  donorId: number;
  firstName: string;
  lastName: string;
  middleName: string;
  birthDate: Date;
  totalDonation: number;
  deferralCode: number;
  deferralCodeKey: string;
  deferralCreateDate: string;
  unitNumber: string;
  locationNumber: number;
  locationName: string;
  regionId: number;
  regionName: string;
  externalId: string;
  reviewStatusKey: string;
  reviewDecisionKey: string;
  reviewEmployeeId: string;
  reviewEmployeeName: string;
  detailedInfo: any;
}
