export enum ProductCategory {
    FROZEN,
    REFRIGERATED,
    ROOM_TEMPERATURE,
}

export const ProductCategoryMap: Record<keyof typeof ProductCategory, string> =
    {
        FROZEN: 'Frozen',
        REFRIGERATED: 'Refrigerated',
        ROOM_TEMPERATURE: 'Room Temperature',
    };
