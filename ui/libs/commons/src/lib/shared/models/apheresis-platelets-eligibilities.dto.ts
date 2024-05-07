export interface ApheresisPlateletsEligibilitiesDto {
  [index: number]: {
    orderNumber: number;
    active: boolean;
    createDate: Date;
    modificationDate: Date;
    id: number;
    task: string;
    deviceTypeKey: string;
    pasSolution: boolean;
    outcome: string;
    plateletEligibilityItems: {
      [index: number]: {
        task: string;
        itemKey: string;
        itemValue: string;
        orderNumber: number;
        id: number;
        plateletEligibilityId: number;
      };
    };
  };
}
