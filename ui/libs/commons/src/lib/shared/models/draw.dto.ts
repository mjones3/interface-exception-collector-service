export interface DrawDto {
  machineId?: number;
  machineSerialNumber?: string;
  arm?: string;
  drawDate?: Date | string;
  drawHours?: number;
  withdrawlDate?: Date;
  noRinseback: string;
  bagTypeId: string;
  bagTypeKey: string;
  bagCount?: number;
  donationId: number;
  lotNumber: string;
  createDate: Date;
  properties?: { [key: string]: string };
  deleteDate?: Date;
  discardUnit?: boolean;
  actualDonationTypeKey?: string;
  qualityControlKey?: string;
  preTubeCount?: number;
  postTubeCount?: number;
  preEmployeeId?: string;
  postEmployeeId?: string;
}
