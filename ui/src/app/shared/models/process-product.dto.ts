export interface ProcessProductDto {
  id: string;
  descriptionKey: string;
  active: boolean;
  orderNumber: number;
  properties: Map<string, string>;
}
