export interface WaferLotNumberDto {
  id?: number;
  lotNumber?: string;
  expirationDate?: string;
  activationDate?: string;
  inactivationDate?: string;
  status?: string;
  facilityId?: number;
  typeId?: number;
}
