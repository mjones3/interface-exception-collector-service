import { Option } from './option.model';

export type ControlFormType = 'select' | 'input';

export interface Control {
  controlName?: string;
  controlLabel?: string;
  result?: any;
  controlType?: ControlFormType; // Default 'select'
  options?: Option[]; // If select provide options
  optionsLabel?: string;

  [key: string]: any;
}
