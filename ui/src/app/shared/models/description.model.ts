export type DescriptionValueType = 'badge' | null | undefined;

export interface Description {
    label: string;
    value?: string | string[] | number;
    valueType?: DescriptionValueType;
    valueCls?: string;
    valueStyle?: Record<string, any> | Record<string, any>[];
    key?: string;
}
