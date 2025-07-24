export const USE_CHECK_DIGIT = 'USE_CHECK_DIGIT';

export interface IrradiationResolveData {
    showCheckDigit: boolean;
}
export interface CheckDigitResponseDTO {
    isValid: boolean;
}

export interface RecordVisualInpectionResult {
    irradiated: boolean;
    comment: string;
}

export interface ReasonDTO {
    type: string;
    reasonKey: string;
    consequenceType: string;
    priority: number;
}

export interface VisualInspectionRequestDTO {
    status: string;
    comments: string;
    reasons: ReasonDTO[];
}

export interface ValidationDataDTO {
    unitNumber: string;
    productCode: string;
}

export const MessageType = {
    SUCCESS: 'SUCCESS',
    WARNING: 'WARNING',
    ERROR: 'ERROR'
} as const;

export type MessageType = keyof typeof MessageType;

export interface IrradiationProductDTO {
    unitNumber: string;
    productCode: string;
    productDescription: string;
    productFamily: string;
    location: string;
    comments: string;
    statusReason: string;
    unsuitableReason: string;
    status: string;
    statusClasses?: string;
    icon?: string;
    order: number;
    visualInspection?: VisualInspectionRequestDTO;
    statuses?: { value: string; classes: string }[];
    disabled?: boolean;
    expired: boolean;
    alreadyIrradiated: boolean,
    notConfigurableForIrradiation: boolean,
    isBeingIrradiated: boolean,
    quarantines: IrradiationProductQuarantineDTO[];
}

export interface IrradiationProductQuarantineDTO {
    reason: string;
    comments: string;
    stopsManufacturing: boolean;
}

export interface ReadConfigurationDTO {
    key: string;
    value: string;
}

export interface ValidateUnitEvent {
    unitNumber: string;
    checkDigit: string;
    scanner: boolean;
}

export interface StartIrradiationBatchItemDTO {
    unitNumber: string;
    productCode: string;
    lotNumber: string
}

export interface StartIrradiationSubmitBatchRequestDTO {
    deviceId: string;
    startTime: string;
    batchItems: StartIrradiationBatchItemDTO[];
}

export interface StartIrradiationSubmitBatchResponseDTO {
    submitBatch: SubmitBatchDTO;
}

export interface SubmitBatchDTO {
    message: string;
}
