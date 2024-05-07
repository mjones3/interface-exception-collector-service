import { LookUpDto, ProductCategoryDto } from '@rsa/commons';
import {
  CMV_STATUS,
  HBS_NEGATIVE,
  ImportFacilityIdentification,
  ImportItem,
  LICENSE_STATUS,
  Patient,
} from '@rsa/distribution/core/models/imports.models';

export const productCategoriesMock: ProductCategoryDto[] = [
  {
    id: 1,
    descriptionKey: 'freezing.label',
    active: true,
    orderNumber: 1,
    type: 'LABEL_SELECTION',
  },
  {
    id: 2,
    descriptionKey: 'room-temperature.label',
    active: true,
    orderNumber: 1,
    type: 'LABEL_SELECTION',
  },
];

export const timeZonesMock: LookUpDto[] = [
  {
    id: 1,
    descriptionKey: 'central-america-standard-time.label',
    optionValue: 'CAST',
    active: true,
    type: 'RETURNS_TRANSIT_TIME_ZONE',
  },
  {
    id: 2,
    descriptionKey: 'Central Standard Time',
    optionValue: 'CDT',
    active: true,
    type: 'RETURNS_TRANSIT_TIME_ZONE',
  },
];

export const inspectionsMock: LookUpDto[] = [
  { id: 1, optionValue: 'acceptable', descriptionKey: 'acceptable.label', active: true, type: '' },
  { id: 2, optionValue: 'unacceptable', descriptionKey: 'unacceptable.label', active: true, type: '' },
];

export const productListMock: ImportItem[] = [
  {
    id: 1,
    unitNumber: 'W123456789123',
    bloodType: 'ABP',
    isbtProductCode: 'E1234V00',
    descriptionKey: 'Description',
    expirationDate: '02/05/2022',
    patientRecord: true,
    facilityIdentification: {
      fin: 'W1234',
      name: 'name',
      city: 'city',
      country: 'country',
      registrationNumber: '12345',
      licenseNumber: '12345',
      orderNumber: 1,
      active: true,
    },
    itemAttributes: [
      { propertyKey: LICENSE_STATUS, propertyValue: 'unlicensed.label' },
      { propertyKey: CMV_STATUS, propertyValue: 'cmv-positive.label' },
      { propertyKey: HBS_NEGATIVE, propertyValue: true },
    ],
    returnItemConsequences: [],
  },
  {
    id: 2,
    unitNumber: 'W123456789127',
    bloodType: 'ON',
    isbtProductCode: 'E1235V00',
    descriptionKey: 'Description',
    expirationDate: '02/05/2022',
    patientRecord: false,
    facilityIdentification: {
      fin: 'W1234',
      name: 'name',
      city: 'city',
      country: 'country',
      registrationNumber: '12345',
      licenseNumber: '12345',
      orderNumber: 1,
      active: true,
    },
    itemAttributes: [{ propertyKey: LICENSE_STATUS, propertyValue: 'unlicensed.label' }],
    returnItemConsequences: [],
  },
  {
    id: 3,
    unitNumber: 'W123456789124',
    bloodType: 'ABP',
    isbtProductCode: 'E1236V00',
    descriptionKey: 'Description',
    expirationDate: '02/05/2022',
    patientRecord: true,
    facilityIdentification: {
      fin: 'W1234',
      name: 'name',
      city: 'city',
      country: 'country',
      registrationNumber: '12345',
      licenseNumber: '12345',
      orderNumber: 1,
      active: true,
    },
    itemAttributes: [{ propertyKey: LICENSE_STATUS, propertyValue: 'unlicensed.label' }],
    returnItemConsequences: [],
  },
  {
    id: 4,
    unitNumber: 'W123456789125',
    bloodType: 'BP',
    isbtProductCode: 'E1237V00',
    descriptionKey: 'Description',
    expirationDate: '02/05/2022',
    patientRecord: false,
    facilityIdentification: {
      fin: 'W1234',
      name: 'name',
      city: 'city',
      country: 'country',
      registrationNumber: '12345',
      licenseNumber: '12345',
      orderNumber: 1,
      active: true,
    },
    itemAttributes: [
      { propertyKey: LICENSE_STATUS, propertyValue: 'licensed.label' },
      { propertyKey: CMV_STATUS, propertyValue: 'cmv-negative.label' },
    ],
    returnItemConsequences: [],
  },
];

export const patientListMock: Patient[] = [
  {
    id: 1,
    firstName: 'john',
    lastName: 'smith',
    dob: '2021-01-29',
  },
  {
    id: 2,
    firstName: 'john',
    lastName: 'smith',
    dob: '2021-01-29',
  },
  {
    id: 3,
    firstName: 'jane',
    lastName: 'doe',
    dob: '2021-01-30',
  },
];

export const importFacilitiesMock: ImportFacilityIdentification[] = [
  {
    id: 1,
    fin: '123',
    licenseNumber: '123',
    registrationNumber: '123',
    name: 'John',
    city: 'Miami',
    state: 'Florida',
    country: 'USA',
    postalCode: '111111',
    orderNumber: 0,
    active: true,
    createDate: '2022-05-18',
    modificationDate: '2022-05-18',
  },
  {
    id: 2,
    fin: '456',
    licenseNumber: '456',
    registrationNumber: '456',
    name: 'Jane',
    city: 'Miami',
    state: 'Florida',
    country: 'USA',
    postalCode: '111111',
    orderNumber: 0,
    active: true,
    createDate: '2022-05-18',
    modificationDate: '2022-05-18',
  },
];
