export interface PositiveBactFollowUpDto {
  id: number;
  donationId: number;
  inventoryId: number;
  locationId: number;
  pqcTestResultId: number;
  pqcTestType: string;
  gramStain?: string;
  organismId?: string;
  sampleStatus: string;
  sampleStatusComment?: string;
  testResultStatus: string;
  systemInterpretation: string;
  appliedInterpretation?: boolean;
  comment?: string;
  closeComment?: string;
  createEmployeeId: string;
  closeEmployeeId?: number;
  closeDate?: Date;
  deleteDate?: Date;
  createDate: Date;
}
