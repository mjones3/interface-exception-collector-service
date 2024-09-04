export interface DiscardRequestDTO {
    unitNumber: string;
    productCode: string;
    reasonDescriptionKey: string;
    locationId: string;
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
    locationId: string;
    employeeId: string;
    createDate: string;
}
