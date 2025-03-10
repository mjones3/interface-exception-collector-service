export interface CreateExternalTransferRequestDTO {
    id?: number;
    customerCode: string;
    transferDate: string;
    hospitalTransferId?: string;
    createEmployeeId: string;
}
export interface CustomerOptionDTO {
    code: string;
    name: string;
}

export interface ExternalTransferCustomerDTO {
    code: string;
    name: string;
}

export interface ExternalTransferResponseDTO {
    id: number;
    customerTo: ExternalTransferCustomerDTO;
    customerFrom: ExternalTransferCustomerDTO;
    createEmployeeId: string;
    hospitalTransferId?: string;
    status: string;
    externalTransferItems?: ExternalTransferItemDTO[];
}
export interface ExternalTransferItemDTO {
    externalTransferId: number;
    unitNumber: string;
    productCode: string;
    productFamily?: string;
    employeeId: string;
}
