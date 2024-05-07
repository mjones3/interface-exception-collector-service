export interface DhqDeferredDonorReviewDto {
  id?: number;
  dhqDeferredDonorId: number;
  statusKey: string;
  decisionKey?: string;
  employeeId?: string;
  locationId?: number;
  createDate?: Date;
  modificationDate?: Date;
  deleteDate?: Date;
  clientTimezone?: string;
  employeeName?: string;
}
