export type DescriptionValueType = 'badge' | null | undefined;

export interface Description {
  label: string;
  value?: string | string[];
  valueType?: DescriptionValueType;
  valueCls?: string;
  valueStyle?: { [key: string]: any } | { [key: string]: any }[];
  key?: string;
}
