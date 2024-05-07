// Theme type
export type Theme = 'auto' | 'dark' | 'light' | 'admin-light' | 'distribution-light' | 'specialty-lab-light';

export type LayoutType =
  | 'empty'
  | 'centered'
  | 'enterprise'
  | 'material'
  | 'modern'
  | 'basic'
  | 'classic'
  | 'classy'
  | 'compact'
  | 'dense'
  | 'futuristic'
  | 'thin';

export interface AppConfig {
  [key: string]: any;

  name?: string;
  description?: string;
  layout: LayoutType;
  theme: Theme;
}

export interface Layout {
  [key: string]: any;

  collapsed: boolean;
  lang: string;
  layout: LayoutType;
}

export interface Title {
  [key: string]: any;

  title: boolean;
  subTitle: string;
}
