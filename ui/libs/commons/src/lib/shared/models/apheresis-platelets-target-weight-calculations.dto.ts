export interface ApheresisPlateletsTargetWeightCalculationsDto {
  [index: number]: {
    orderNumber: number;
    active: boolean;
    createDate: Date;
    modificationDate: Date;
    id: number;
    pathway: string;
    deviceTypeKey: string;
    pasSolution: boolean;
    bagTypeKey: string;
    bagLetter: string;
    formula: string;
  };
}

export interface ApheresisPlateletsTargetWeightCalculationsRequest {
  pathway: string;
  deviceTypeKey: string;
  pasSolution: boolean;
  bagTypeKey: string;
  bagLetter: string;
  targetVolume: string;
  numberOfProducts: number;
  task: string;
}
