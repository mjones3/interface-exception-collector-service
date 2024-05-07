export interface ApheresisPlateletsRwbcEligibilitiesDto {
  [index: number]: {
    orderNumber: number;
    active: boolean;
    createDate: Date;
    modificationDate: Date;
    id: number;
    task: string;
    outcome: string;
    rwbcEligibilityItems: any;
  };
}
