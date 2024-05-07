export interface WorkstationDto {
  id: number;
  name: string;
  active?: boolean;
  locationId?: number;
  createDate?: Date;
  modificationDate?: Date;
  orderNumber?: number;
}
