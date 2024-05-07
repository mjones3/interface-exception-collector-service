import { PrescriptionDonationDto } from './prescription-donation.dto';

export interface Prescription {
  id?: number;
  patientId?: number;
  practitionerId?: number;
  hospitalId?: number;
  dateOfUse?: Date;
  createDate?: Date;
  deleteDate?: Date;
  modificationDate?: Date;

  properties?: Set<PrescriptionDonationDto>;
}
