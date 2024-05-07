export interface BagTypeDto {
  id: string;
  description: string;
  bagNumber: string;
  bagVolume: number;
  orderNum: number;
  active: boolean;
  descriptionKey?: string;
  anticoagulant?: string;
  displayPas?: string;
  displayThirdBag?: string;
}
