export interface AntigenDonorStatusSummaryDto {
  donorId: number;
  confirmedResults: AntigenDonorStatusSummaryDetailDto[];
  unconfirmedResults: AntigenDonorStatusSummaryDetailDto[];
}

export interface AntigenDonorStatusSummaryDetailDto {
  id: number;
  donorId: number;
  reagentId: number;
  reagentDescriptionKey: string;
  parentHtmlTag: string;
  testResult: string;
  confirmDate: string;
  confirmDateTimeZone: string;
  licenseDate: string;
}
