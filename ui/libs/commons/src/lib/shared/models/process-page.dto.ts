export interface ProcessPageDto {
  id: string;
  descriptionKey: string;
  active: boolean;
  orderNumber: number;
  properties: Map<string, string>;
}
