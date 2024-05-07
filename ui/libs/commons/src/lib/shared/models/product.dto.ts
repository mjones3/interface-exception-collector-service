export interface ProductDto {
  orderNumber?: number;
  active?: boolean;
  createDate?: string;
  modificationData?: string;
  productCode?: string;
  descriptionKey?: string;
  isbtDescriptionKey?: string;
  status?: string;
  bagCode?: string;
  isbtCode?: string;
  expiryTime?: string;
  startOfExpirationTime?: StartOfExpirationTimeOptions;
  properties?: { [key: string]: string };
  shortDescriptionKey?: string;
  fromDrawDate?: boolean;
  icon?: string; //Should be removed since it doesn't exist on DTO
}

export enum StartOfExpirationTimeOptions {
  DRAW_DATE = 'DRAW_DATE',
  DRAW_TIME = 'DRAW_TIME',
  DEGLYCEROLIZATION_TIME = 'DEGLYCEROLIZATION_TIME',
  IRRADIATION_DATE = 'IRRADIATION_DATE',
  RAPID_BACTERIAL_TEST_DATE = 'RAPID_BACTERIAL_TEST_DATE',
  RECONSTITUTION_DATE = 'RECONSTITUTION_DATE',
  RECONSTITUTION_TIME = 'RECONSTITUTION_TIME',
  THAW_DATE = 'THAW_DATE',
  THAW_TIME = 'THAW_TIME',
  WASH_TIME = 'WASH_TIME',
}
