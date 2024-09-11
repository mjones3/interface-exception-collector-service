export interface DiscardRequestDTO {
    unitNumber: string;
    productCode: string;
    reasonDescriptionKey: string;
    locationCode: string;
    comments: string;
    triggeredBy: string;
    employeeId: string;
    productShortDescription: string;
    productFamily: string;
}

export interface DiscardResponseDTO {
    id: number;
    unitNumber: string;
    productCode: string;
    productShortDescription: string;
    productFamily: string;
    locationCode: string;
    employeeId: string;
    createDate: string;
}
