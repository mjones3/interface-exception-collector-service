export interface DonationReviewDto {
  id?: number;
  donationId: number;
  reviewRuleKey?: string;
  resolvedDate?: string;
  resolvedReasonKey?: string;
  status: string;
  resolvedEmployeeId?: string;
  createDate?: string;
  deleteDate?: string;
  modificationDate?: string;
}

export interface DonationReviewReportDto {
  donationReviewReportId?: number;
  unitNumber?: string;
  donationType?: string;
  dateOfDiscovery?: string;
  collectionLocation?: string;
  regionId?: number;
  regionName?: string;
  status?: string;
  driveId?: string;
  collectionLocationId?: number;
  donationId?: number;
}
