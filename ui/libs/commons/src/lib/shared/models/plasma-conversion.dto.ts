export interface PlasmaConversionDto {
  id: number; //Product available selected id
  toProductCode: string;
  type: 'THAWED' | 'INVENTORY_CONTROL';
  reasonId?: number;
  reasonDescriptionKey?: string;
  comment?: string;
  conversionDate?: string;
  overrideExpired?: boolean;
}
