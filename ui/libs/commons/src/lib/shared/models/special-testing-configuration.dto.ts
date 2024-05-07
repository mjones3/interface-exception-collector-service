export interface SpecialTestingConfigurationDto {
  id?: number;
  regionId?: number;
  specialTestingConfigurationCriteriaId?: number;
  opQuantity?: number;
  onQuantity?: number;
  anQuantity?: number;
  apQuantity?: number;
  bnQuantity?: number;
  bpQuantity?: number;
  abnQuantity?: number;
  abpQuantity?: number;
  bagTypes?: string[];
  donorTypes?: string[];
  ethnicities?: string[];
  races?: string[];
}

export interface SpecialTestingConfigurationSearch {
  regionId?: number;
  specialTestingConfigId?: number;
}

export interface SpecialTestingConfigurationCriteriaDto {
  id?: number;
  descriptionKey?: string;
  descriptionHtmlTag?: string;
  orderNumber?: number;
  active?: boolean;
  createDate?: Date;
  modificationDate?: Date;
}
