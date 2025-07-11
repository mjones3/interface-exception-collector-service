export enum ShipmentType {
    CUSTOMER,
    INTERNAL_TRANSFER ,
    RESEARCH_PRODUCTS
}

export const ShipmentTypeMap: Record<keyof typeof ShipmentType, string> = {
    CUSTOMER: 'Customer',
    INTERNAL_TRANSFER: 'Internal Transfer',
    RESEARCH_PRODUCTS: 'Research Products'
};
