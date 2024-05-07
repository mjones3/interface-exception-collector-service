export interface DonorContactPointDto {
  id?: number;
  donorId: number;
  useKey: string;
  systemKey: string;
  value: string;
  extension?: string;
  orderNumber?: number;
}
