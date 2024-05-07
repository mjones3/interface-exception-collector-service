export interface BacterialSampleTimeReportDto {
  inventoryId: number;
  poolNumber: string;
  plateletsCount: string;
  plateletsYield: string;
  storageLocation: string;
  elapsedTime: string;
  readyToSampleTime: Date;
  sampleTimeStatus: string;
}
