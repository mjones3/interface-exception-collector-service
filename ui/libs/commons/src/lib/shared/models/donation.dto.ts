export interface DonationDto {
  unitNumber?: string;
  externalId?: string;
  donorId?: number;
  locationId?: number;
  isReviewed?: boolean;
  donationTypeId?: number;
  motivationId?: number;
  donationTypeKey?: string;
  motivationKey?: string;
  id?: number;
  donationDate?: Date;
  discardDate?: Date;
  deleteDate?: Date;
  createDate?: Date;
  // added any property because it is coming an object from the endpoint
  // this should be verified in the future.
  properties?: Map<string, string> | any;
  tripNumber?: number;
  photoIdPresented?: boolean;
  downtimeForm?: boolean;
  status?: string;
  calculatedStatus?: string;
  employeeId?: string;
  relationshipKey?: string;
  languageCode?: string;
  isPoolUnit?: boolean; //TODO: Change to the correct attribute
  donationReviewId?: number;
}

export interface DonationDiscardDto {
  id?: number;
  donationId: number;
  reasonId?: number;
  reasonKey?: string;
  employeeId: string;
  comments?: string;
  createDate?: string;
  modificationDate?: string;
  deleteDate?: string;
}

export interface EarlyDonationDto {
  id?: number;
  donationId: number;
  unitNumber: string;
  quarantineId: number;
  resolvedDate?: string;
  resolvedEmployeeId?: string; //maxLength: 50
  resolvedReasonKey?: string; //maxLength: 255
  status: string;
  deleteDate?: string;
  createDate?: string;
  modificationDate?: string;
}

export interface EarlyDonationReportDto {
  earlyDonationId?: number;
  unitNumber?: string;
  donorIntention?: string;
  donationType?: string;
  regionId?: number;
  regionName?: string;
  dateOfDiscovery?: string;
  status?: string;
}

export interface EarlyDonationResolveDTO {
  earlyDonationId: number;
  status: string;
  discard?: boolean;
  deactivateQuarantine?: boolean;
  comments?: string;
  resolveReasonKey?: string;
}
