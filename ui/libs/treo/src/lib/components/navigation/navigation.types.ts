export interface TreoNavigationItem {
  id?: number;
  title?: string;
  subtitle?: string;
  type: string;
  hidden?: (item: TreoNavigationItem) => boolean;
  active?: boolean;
  disabled?: boolean;
  link?: string;
  externalLink?: boolean;
  exactMatch?: boolean;
  function?: (item: TreoNavigationItem) => void;
  scanBadge?: boolean;
  classes?: string;
  icon?: string;
  iconClasses?: string;
  badge?: {
    title?: string;
    style?: 'rectangle' | 'rounded' | 'simple';
    background?: string;
    color?: string;
  };
  children?: TreoNavigationItem[];
  meta?: any;
}

export type TreoVerticalNavigationAppearance = string;
export type TreoVerticalNavigationMode = 'over' | 'side';
export type TreoVerticalNavigationPosition = 'left' | 'right';
