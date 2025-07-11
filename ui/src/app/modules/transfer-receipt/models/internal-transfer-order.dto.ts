import { LookUpDto } from "@shared";

export interface TransferInformationDTO {
    orderNumber: string;
    temperatureUnit: string;
    displayTransitInformation: boolean;
    displayTemperature: boolean;
    transitTimeZoneList: LookUpDto[];
    visualInspectionList: LookUpDto[];
    defaultTimeZone: string;
}