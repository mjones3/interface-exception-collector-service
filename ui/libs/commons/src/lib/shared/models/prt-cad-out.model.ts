export class CadOutBagLetterOption {
  id: number;
  parentId: number;
  productCode: string;
  descriptionKey: string;
  donationId: number;
  status: string;
  processIndex: string;
  isQuarantine: boolean;
  properties: {
    PRT_KIT: string;
    TARGET_WEIGHT: number;
    VOLUME: number;
    BAG_LETTER: string;
    QC_OPTIONS_SELECTED: any[];
    SAMPLE_VOLUME_LOSS: number;
    QC: string;
    SAMPLE_TASK_KEYS: string;
    TARGET_VOLUME: number;
    TOTAL_PRODUCT_COUNT: number;
    PATHWAY: string;
    VISUAL_INSPECTION: string;
    SAMPLE_TIME: string;
    YIELD_OUTCOME: string;
    WEIGHT: number;
  };
  unitNumber: string;
  discardDate: string;
  deleteDate: string;
  createDate: string;
  modificationDate: string;
  notifications: any[];
  facilityId: number;
  currentFacilityId: number;
  cad: {
    isCadOutEligible: boolean;
    processTime: string;
    processStatusType: string;
    processStatus: string;
  };
}
