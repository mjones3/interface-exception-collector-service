import { ProductDto } from './product.dto';

export interface ProductConversionDto {
  id: number;
  conversionType: string;
  fromProduct: string;
  toProduct: string;
  product: ProductDto;
  volumeMinValue: number;
  volumeMaxValue: number;
}
