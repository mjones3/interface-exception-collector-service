export interface ConversionMappingDto {
  id?: number;
  type: string;
  valueFrom: string;
  labelFrom: string;
  valueTo: string;
  labelTo: string;
  active: boolean;
  createDate?: Date;
  modificationDate?: Date;
}
