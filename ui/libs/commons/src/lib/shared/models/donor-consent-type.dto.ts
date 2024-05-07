export interface DonorConsentTypeDto {
  id: number;
  descriptionKey: string;
  orderNumber: number;
  active: boolean;
}

export interface DonorConsentsDto {
  id: number;
  consentTypeId: number;
  donationId: number;
  employeeId: string;
  deleteDate: Date;
  createDate: Date;
  consentSignatures: [];
}
