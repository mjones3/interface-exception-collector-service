import { ProductDto } from './product.dto';

export interface ProductCategoryDto {
  id: number;
  descriptionKey: string;
  type: string;
  orderNumber: number;
  active: boolean;
  createDate?: Date;
  modificationDate?: Date;
  productCodes?: string[];
  products?: ProductDto[];
  codes?: string[];
  subtitleLabel?: string;
}
