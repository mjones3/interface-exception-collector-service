
export interface AddImportItemRequestDTO {
    importId: number,
    unitNumber: string,
    productCode: string,
    aboRh: string,
    expirationDate: string,
    visualInspection: string,
    licenseStatus: string,
    employeeId: string
}

export interface ImportedItemResponseDTO{
    id: number,
    importId: number,
    unitNumber: string,
    productCode: string,
    productDescription: string,
    aboRh: string,
    expirationDate: string,
    visualInspection: string,
    isQuarantined?: boolean,
    licenseStatus: string
}

export interface CreateImportResponsetDTO {
    id: number,
    maxNumberOfProducts: number,
    temperatureCategory: string,
    transitStartDateTime:string,
    transitStartTimeZone:string,
    transitEndDateTime:string,
    transitEndTimeZone:string,
    temperature:number,
    thermometerCode:string,
    locationCode:string,
    isQuarantined: boolean,
    comments:string,
    employeeId:string,
    products: ImportedItemResponseDTO[]
}