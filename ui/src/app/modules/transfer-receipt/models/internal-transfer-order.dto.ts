import { LookUpDto } from "@shared";

export interface TransferInformationDTO {
    productCategory: string;
    receivedDifferentLocation: boolean;
    orderNumber: string;
    temperatureUnit: string;
    displayTransitInformation: boolean;
    displayTemperature: boolean;
    transitTimeZoneList: LookUpDto[];
    visualInspectionList: LookUpDto[];
    defaultTimeZone: string;
}