export interface ProductFieldDto {
  id: number;
  descriptionKey: string;
  productCode: string;
  type: string;
  minValue: number;
  maxValue: number;
  precision: number;
  orderNumber: number;
  active: boolean;
}
