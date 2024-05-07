export interface DonorAdverseEventDto {
  id?: number;
  donorId: number;
  donationId: number;
  unitNumber:	string;
  event: string;
  eventDate: string;
  createDate?: string;
  modificationDate?: string;
}
