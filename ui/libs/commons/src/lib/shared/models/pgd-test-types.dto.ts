export interface PgdTestTypesDto {
  id?: number;
  descriptionKey: string;
  expirationInterval: string;
  expirationEndOfDay: string;
  orderNumber: number;
  active: boolean;
  pgdReagents?: [];
  pgdControls?: [];
  createDate?: Date;
  modificationDate?: Date;

  [key: string]: any;
}
