export interface PhysicianOverrideDto {
  id: number;
  donationId?: number;
  typeKey?: string;
  employeeId?: string;
  overrideEmployeeId?: number;
  overrideComments?: string;
  practitionerId?: number;
  practitionerComments?: string;
  defer?: boolean;
  deleteDate?: Date;
  createDate: Date;
  screenName: string;
}
