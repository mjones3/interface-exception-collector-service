export interface DonationDonorReportDto {
  id: number;
  referenceId?: number;
  duplicateDonorName: string;
  unitNumber?: string;
  drawDate?: Date;
  possibleDuplicateDonorName?: string;
  dateOfDiscovery?: Date;
  status?: string;
}
