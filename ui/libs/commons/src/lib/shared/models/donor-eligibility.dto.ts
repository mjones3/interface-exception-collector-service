export interface DonorEligibilityDto {
  id: number;
  donorId: number;
  motivationKey: string;
  donationTypeKey: string;
  eligibleDate: string | Date;
  orderNumber?: number;
  createDate?: string | Date;
  modificationDate?: string | Date;
  deleteDate?: string | Date;
}
