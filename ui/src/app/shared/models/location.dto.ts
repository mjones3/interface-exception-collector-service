export interface LocationDto {
  id?: number;
  locationTypeId?: number;
  partOfId?: number;
  name?: string;
  description?: string;
  orderNum?: number;
  active?: boolean;
  properties?: object;
  locationTypeIds?: number[];
}

export interface LocationGroupDto {
  id?: number;
  name?: string;
  description?: string;
  groupType?: string;
  locationIds?: number[];
}

export interface LocationAddressDto {
  id?: number;
  locationId: number;
  useKey?: string;
  typeKey: string;
  city?: string;
  state: string;
  postalCode: string;
  country: string;
  countryCode: string;
  screenName?: string;
  lines?: string[];
  orderNumber?: number;
  active: boolean;
  name?: string;
  description?: string;
  groupType?: string;
  locationIds?: number[];
}
