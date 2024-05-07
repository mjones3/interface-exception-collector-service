export interface CentrifugeInventoryDto{
    id?: number;
    centrifugeId: number;
    inventoryId: number;
    centrifugeTypeId: number;
    deleteDate?: Date;
    createDate?: Date;
}
