export interface DeferralDto {
  id?: number;
  code?: string;
  codeKey: string;
  donorId?: number;
  donationId?: number;
  employeeId?: string;
  deactivateDate?: string;
  deactivateEmployeeId?: string;
  comments?: string;
  deferralDate: string;
  expirationDate: string;
  deleteDate?: string;
  createDate?: string;
  deactivateComments?: string;
  deactivateReasonKey?: string;
  firstEventDate?: string;
  visitId?: string;
  unitNumber?: string;
  status?: string;
}
