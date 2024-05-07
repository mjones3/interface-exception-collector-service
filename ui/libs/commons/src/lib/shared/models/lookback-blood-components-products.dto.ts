export interface LookbackBloodComponentsProductsDto {
  donationId: number;
  donorId: number;
  unitNumber: string;
  inventoryId: number;
  productCode: string;
  donationDate: string;
  donorIntention: string;
  donationType: string;
  productDescription: string;
  collectionLocation: string;
  orderNumber?: string;
  shipToConsigneeName?: string;
  poolNumbersAndStatusesLoaded: boolean;
}
