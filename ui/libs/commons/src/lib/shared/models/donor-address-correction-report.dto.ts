export interface DonorAddressCorrectionReportDto {
  addressAuditId?: number;
  donorId: number;
  donorAddressId?: number;
  addressCorrectionId?: number;
  firstName: string;
  lastName: string;
  dateOfDiscovery: string;
  status: string;
}
