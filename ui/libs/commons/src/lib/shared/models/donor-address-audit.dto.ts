export interface DonorAddressAuditDto {
  id: number;
  donorId: number;
  donorAddressId: number;
  type: string;
  validationStatus?: string;
  useKey?: string;
  typeKey?: string;
  city?: string;
  district?: string;
  state?: string;
  postalCode?: number;
  country?: string;
  lines?: string[];
  properties?: { [key: string]: string };
  screenName?: string;
  orderNumber?: number;
  createDate: string;
  deleteDate: string;
}
