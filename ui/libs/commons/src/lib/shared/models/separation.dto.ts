import {ProductDto} from './product.dto';

export interface SeparationType {
  type: 'SOURCE_LEUKOCYTES' | 'RBC_PRP' | 'PLATELET_PLASMA' | 'RBC_PLASMA' | 'CRYO_PLASMA';

  [key: string]: any;
}

export interface SeparationDto {
  facilityId: number;
  separationProcess?: SeparationType;
  productsToCreate: ProductDto[];
}



