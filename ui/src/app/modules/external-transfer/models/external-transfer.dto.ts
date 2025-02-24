export interface CreateTransferInfoDTO {
    id?: number;
    customerCode: string;
    transferDate: string;
    hospitalTransferId?: string;
    createEmployeeId: string;
}

export interface customerOptionDto {
    code: string;
    name: string;
}
