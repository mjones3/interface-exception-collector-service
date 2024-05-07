export interface PgdReagentControlDto {
  id?: number;
  facilityId: number;
  testTypeId: number;
  testTypeKey: string;
  expirationDate?: Date;
  lockDate?: Date;
  createDate?: Date;
  modificationDate?: Date;
  deleteDate?: Date;

  [key: string]: any;
}
