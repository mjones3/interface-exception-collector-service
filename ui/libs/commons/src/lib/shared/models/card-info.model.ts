export interface CardInfo {
  key?: string;
  descriptionKey: string;
  value: any;
  action?: CardAction;
}

export interface CardAction {
  icon?: string;
  click?: Function;
  disabled?: boolean;
}

export interface CardField {
  [prop: string]: string;
}

export interface NGClass {
  [prop: string]: boolean;
}

export interface MultipleInfoData {
  title: string;
  data: CardInfo[];
}
