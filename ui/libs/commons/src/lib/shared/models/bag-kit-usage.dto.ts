export interface BagKitUsageDto {
  id: number;
  donationId: number;
  lotNumber: number;
  bagTypeId: number;
  bagTypeKey: string;
  failureReasonId?: number;
  failureReasonKey?: string;
  expirationDate: Date;
  failureDate?: Date;
  deleteDate?: Date;
  createDate: Date;
}
