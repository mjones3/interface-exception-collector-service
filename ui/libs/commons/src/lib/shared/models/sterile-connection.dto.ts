export interface SterileConnectionDto {
  id?: number;
  inventoryId?: number;
  typeId?: number;
  employeeId?: string;
  typeKey?: string;

  //Sterile Connection Attributes
  serialNumber?: string;
  transferLot?: string;
  waferLot?: string;

  //Weld Inspection Attributes
  weldInspectionKey?: string;
  reWeldKey?: string;
  comments?: string;
  deleteDate?: Date;

  processIndex?: string;
  currentLotNumber?: string;
  currentProcessIndex?: string;
  visualInspectionKey?: string;
}
