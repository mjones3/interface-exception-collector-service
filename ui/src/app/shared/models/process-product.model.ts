export interface ProcessProductModel {
    id: string;
    descriptionKey: string;
    active: boolean;
    orderNumber: number;
    properties: Map<string, string>;
}
