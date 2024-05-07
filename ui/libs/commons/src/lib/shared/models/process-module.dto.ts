export interface ProcessModuleDto {
  id: string;
  descriptionKey: string;
  active: boolean;
  orderNumber: number;
  properties: Map<string, string>;
}
