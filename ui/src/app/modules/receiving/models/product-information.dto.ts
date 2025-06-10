
export interface ImportedProductInformationDTO{
    unitNumber: string,
    productCode: string,
    description: string,
    aboRh: string,
    expirationDate: string,
    visualInspection: string,
    isQuarantined: boolean,
    licenseStatus: string
}


export interface CreateImportRequestDTO {
    temperatureCategory: string,
    transitStartDateTime:string,
    transitStartTimeZone:string,
    transitEndDateTime:string,
    transitEndTimeZone:string,
    temperature:number,
    thermometerCode:string,
    locationCode:string,
    comments:string,
    employeeId:string
}