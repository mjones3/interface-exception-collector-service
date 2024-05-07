export interface EligibilityOverrideDto {
  id: number;
  donationId?: number;
  typeKey?: string;
  employeeId?: string;
  overrideEmployeeId?: string;
  overrideComments?: string;
  defer?: boolean;
  deleteDate?: Date;
  createDate: Date;
  screenName: string;
}
