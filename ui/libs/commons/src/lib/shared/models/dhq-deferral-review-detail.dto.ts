export interface DhqDeferralReviewDetailDto {
  id: number;
  code: string;
  codeKey: string;
  donorId: number;
  donationId?: number;
  employeeId: string;
  deactivateDate?: Date;
  deactivateEmployeeId?: string;
  comments?: string;
  deleteComments?: string;
  deferralDate: Date;
  expirationDate: Date;
  deleteDate?: Date;
  createDate: Date;
  deactivateComments?: string;
  deactivateReasonKey?: string;
  firstEventDate: Date;
  drawDate?: Date;
}
