export interface CodeStyleDto {
  id: number;
  descriptionKey: string;
  code: string;
  codeType: string;
  styleName: string;
  orderNumber: number;
  active: boolean;
  createDate?: Date;
}
