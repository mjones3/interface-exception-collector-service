export enum Reason {
    BROKEN,
    AGGREGATES,
    DEFECTIVE_BAG,
    CLOTTED,
    OTHER,
}

export const ReasonMap: Record<keyof typeof Reason, string> = {
    BROKEN: 'Broken',
    AGGREGATES: 'Aggregates',
    DEFECTIVE_BAG: 'Defective Bag',
    CLOTTED: 'Clotted',
    OTHER: 'Other',
};

export interface ReasonDTO {
    id: number;
    type: string;
    reasonKey: keyof typeof Reason;
    requireComments: boolean;
    orderNumber: number;
    active: boolean;
}
