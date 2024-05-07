export interface DonorAddressCorrectionDto {
  id: number;
  donorId: number;
  donorAddressAuditId: number;
  status: string;
  dateOfDiscovery: string;
  createDate: string;
  createDateTimezone: string;
  modificationDate: string;
  modificationDateTimezone: string;
}

export interface DonorAddressStatusUpdateDto {
  id?: number;
  status: string;
  comments: string;
}
