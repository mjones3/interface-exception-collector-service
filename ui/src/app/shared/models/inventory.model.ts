export interface InventoryDTO {
    id: string;
    locationCode: string;
    unitNumber: string;
    productCode: string;
    productDescription: string;
    expirationDate?: string;
    aboRh: string;
    productFamily: string;
    collectionDate?: string;
    storageLocation?: string;
    createDate?: string;
    modificationDate?: string;
}
