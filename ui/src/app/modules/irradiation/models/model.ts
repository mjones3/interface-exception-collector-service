export const USE_CHECK_DIGIT = 'USE_CHECK_DIGIT';

export interface CheckDigitRequestDTO {
    unitNumber: string;
    checkDigit: string;
}

export interface CheckDigitResponseDTO {
    checkDigit: {
        isValid: boolean;
    };
}

export interface ValidationDataDTO {
    unitNumber: string;
    productCode: string;
    productName: string;
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

export interface CentrifugationProcessDTO {
    id: number;
    descriptionKey?: string;
    icon?: string;
}

export interface IrradiationProductDTO {
    unitNumber: string;
    productCode: string;
    productName: string;
    productFamily: string;
    status: string;
    icon?: string;
    order: number;
    statuses?: { value: string; classes: string }[];
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
    bloodCenterId: string;
    deviceType: string;
    location: string;
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

export interface SubmitIrradiationBatchRequestDTO {
    unitNumbers: string[];
    location: string;
    deviceId: string;
}

export interface CentrifugationResponseDTO {
    batchId: string;
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

export interface ValidateUnitEvent {
    unitNumber: string;
    checkDigit: string;
    scanner: boolean;
}
