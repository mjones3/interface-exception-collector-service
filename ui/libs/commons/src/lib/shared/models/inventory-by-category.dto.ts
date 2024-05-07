export interface InventoryByCategoryDto {
  productCategoryIds: number[];
  productCategoryKeys: string;
  id: number;
  parentId: number;
  donationId: number;
  descriptionKey: string;
  productCode: string;
  locationId: number;
  status: string;
}
