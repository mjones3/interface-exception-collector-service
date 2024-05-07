export interface DonationQuarantineDto {
  id?: number;
  donationId?: number;
  facilityId?: number;
  employeeId?: string;
  employeeName?: string;
  deactivationEmployeeId?: string;
  reasonKey?: string;
  quarantineStatusKey?: string;
  comments?: string;
  deactivationComments?: string;
  drawDate?: string;
  createDate?: string;
  modificationDate?: string;
  deleteDate?: string;
}
