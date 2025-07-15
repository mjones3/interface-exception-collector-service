export const USE_CHECK_DIGIT = 'USE_CHECK_DIGIT';

export interface IrradiationResolveData {
    useCheckDigit: boolean;
}

export interface CheckDigitRequestDTO {
    unitNumber: string;
    checkDigit: string;
}

export interface CheckDigitResponseDTO {
    checkDigit: {
        isValid: boolean;
    };
}

export interface RecordVisualInpectionResult {
    successful: boolean;
    comment: string;
    reasons: ReasonDTO[];
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
    productDescription: string;
    productFamily?: string;
    icon?: string;
    consequence?: ConsequenceDTO;
}

export interface ConsequenceDTO {
    consequenceReasons: string[];
    consequenceType: ConsequenceType;
}

export enum ConsequenceType {
    NONE = 'NONE',
    DISCARD = 'DISCARD',
    QUARANTINE = 'QUARANTINE',
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
    quarantines: IrradiationProductQuarantineDTO[];
}

export interface IrradiationProductQuarantineDTO {
    reason: string;
    comments: string;
    stopsManufacturing: boolean;
}

export interface CentrifugationResolveData {
    useCheckDigit: boolean;
}

export interface ReadConfigurationGraphQL {
    readConfiguration: ReadConfigurationDTO[];
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

export interface DeviceDTO {
    validateDevice: boolean;
}

export interface IrradiationDeviceResponseDTO {
    type: string;
    bloodCenterId: string;
    location: string;
    name: string;
    maxProducts: number;
}

export interface DeviceResponseDTO {
    enterDeviceId: IrradiationDeviceResponseDTO;
}

export interface UnitNumberRequestDTO {
    unitNumber: string;
    location: string;
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

export interface ProductDataDTO {
    unitNumber: string;
    productCode: string;
    productName: string;
    status: string;
    productFamily?: string;
    icon?: string;
}

export interface ValidateUnitNumberResponseDTO {
    products?: ProductDataDTO[];
}

export interface UnitNumberResponseDTO {
    enterUnitNumberForCentrifugation: ValidateUnitNumberResponseDTO;
}

export interface QuarantineDTO {
    stopsManufacturing: boolean;
}
