export interface DonorAddressDto {
  id?: number;
  donorId: number;
  useKey: string;
  typeKey: string;
  city?: string;
  district?: string;
  state: string;
  postalCode: string;
  country: string;
  lines?: string[];
  orderNumber: number;
  deleteDate?: Date;
}
