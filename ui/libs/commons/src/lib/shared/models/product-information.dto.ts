export interface ProductInformationDto {
  id?: number;
  status: string;
  parentId?: number;
  donationId: number;
  productCode: string;
  productDescriptionKey: string;
  volume: string;
  poolNumber: string;
  poolDate: string;
  productCreationDate: string;
  currentFacilityId: number;
  currentLocation?: string;
  labelingDateTime?: string;
  expirationDateTime?: string;
  shippingDateTime?: string;
  shipToCustomer?: string;
  shipmentInformation?: number;
}
