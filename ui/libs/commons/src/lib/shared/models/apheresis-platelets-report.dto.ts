export interface PlateletsWorkloadDTO {
  unitNumber: string;
  productDescription: string;
  storageLocation: string;
  drawDate: string;
  volume: string;
  sampleName: string;
  plateletYieldOutcome: string;
  rwbcOutcome: string;
  plateletsCount: string;
  plateletsYield: string;
  rwbcPlt: string;
  machineType: string;
  storageFluid: string;
  location: string;
}

export interface PlateletsBacterialTestingDTO {
  unitNumber: string;
  productDescription: string;
  storageLocation: string;
  elapsedTime: string;
  bacterialTestingTime: string;
  bacterialTestingTimeRule: string;
}

export interface PlateletsCADTestingDTO {
  unitNumber: string;
  productDescription: string;
  storageLocation: string;
  storageDate: string;
  processTime: string;
  processStatus: string;
  cadOutTime: string;
}
