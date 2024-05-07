export interface Option {
  id?: string;
  name?: string;
  selectionKey?: string;
  icon?: string;
  url?: string;
  disabled?: boolean;
  descriptionKey?: string;
  selected?: boolean;

  [key: string]: any;
}
