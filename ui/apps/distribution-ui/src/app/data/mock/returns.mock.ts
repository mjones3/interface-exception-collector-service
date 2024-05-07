import { LookUpDto, ProductCategoryDto, ReasonDto } from '@rsa/commons';
import { ReturnItem } from '@rsa/distribution/core/models/returns.models';

export const returnReasonsMock: ReasonDto[] = [
  {
    id: 1,
    descriptionKey: 'expired.label',
    active: true,
    orderNumber: 1,
  },
  {
    id: 2,
    descriptionKey: 'broken-bag.label',
    active: true,
    orderNumber: 1,
  },
];

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
    descriptionKey: 'room-remperature.label',
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

export const productListMock: ReturnItem[] = [
  {
    unitNumber: 'W123456789127',
    returnItemConsequences: [],
    inventoryId: 1,
    isbtProductCode: 'E1234V00',
    expired: false,
    positiveBacterialTestingFollowUp: false,
    rareDonor: true,
    quarantined: true,
  },
  {
    unitNumber: 'W123456789123',
    returnItemConsequences: [],
    inventoryId: 2,
    isbtProductCode: 'E1234V00',
    expired: false,
    positiveBacterialTestingFollowUp: false,
    rareDonor: false,
    quarantined: false,
  },
  {
    unitNumber: 'W123456789145',
    returnItemConsequences: [],
    inventoryId: 3,
    isbtProductCode: 'E1234V00',
    expired: false,
    positiveBacterialTestingFollowUp: false,
    rareDonor: false,
    quarantined: false,
  },
  {
    unitNumber: 'W123456789156',
    returnItemConsequences: [],
    inventoryId: 4,
    isbtProductCode: 'E1234V00',
    expired: false,
    positiveBacterialTestingFollowUp: false,
    rareDonor: false,
    quarantined: false,
  },
];
