export interface DonorDto {
  id?: number;
  externalId?: number;
  lastName: string;
  firstName: string;
  middleName?: string;
  gender?: string;
  raceKey?: string;
  languageCode?: string;
  ethnicKey?: string;
  birthDate: string;
  abo?: string;
  rh?: string;
  ssn?: string;
  suffix?: string;
  totalDonations?: number;
  otherDonations?: number;
  deceased: boolean;
  formerId?: number[];
  previousFirstName?: string[];
  previousLastName?: string[];
  zipCode?: string[];
  phoneNumber?: string[];
  description?: string;
  properties?: Record<string, any>;
}

export interface DuplicateDonorDto {
  id?: number;
  newDonorId?: number;
  newDonorName?: string;
  newDonorLastname?: string;
  newDonorMiddleName?: string;
  newDonorFullName?: string;
  unitNumber?: string;
  drawDate?: Date;
  potentialDonorId?: number;
  potentialDonorName?: string;
  potentialDonorLastname?: string;
  potentialDonorMiddleName?: string;
  potentialDonorFullName?: string;
  dateOfDiscovery?: Date;
  status?: string;
  visitId?: string;
  algorithmType?: string;
  activeDeferral?: string;
  region?: string;
  createDate?: Date;
  modificationDate?: Date;
  newDonorSuffix?: string;
  potentialDonorSuffix?: string;
  donationId: number;
}

export interface DuplicateDonorHistoriesDto {
  id?: number;
  duplicateDonorId?: number;
  employeeId?: string;
  status?: string;
  discard?: boolean;
  comments?: string;
  reasonKey?: string;
  fieldNames?: [
    {
      id?: number;
      duplicateDonorHistoryId?: number;
      fieldName?: string;
    }
  ];
  createDate?: Date;
  modificationDate?: Date;
  deleteDate?: Date;
}
