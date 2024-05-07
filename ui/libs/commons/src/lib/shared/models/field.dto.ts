import { FieldOptionsDTO } from './field-options.dto';

export interface FieldDTO {
  id: number;
  descriptionKey: string;
  fieldName: string;
  qcType?: string;
  fieldType: string;
  active?: boolean;
  orderNumber?: number;
  createDate?: Date;
  modificationDate?: Date;
  fieldOptions?: Array<FieldOptionsDTO>;
}
