export interface QuarantineDto {
  id: number;
  inventoryId: number;
  employeeId: number;
  reasonId: number;
  reasonKey: string;
  comment: string;
  createDate: string;
  donationId: number;
  auditAction?: string;
}
