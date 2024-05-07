import { Control } from './dynamic-form.model';

export enum TestStatus {
  POSITIVE = 'test-type-entry.positive.label',
  NEGATIVE = 'test-type-entry.negative.label',
  PENDING_REVIEW = 'pending.label',
  MISSING = 'missing.label',
  COMPLETED = 'completed.label',
}

export interface TestResultEntry {
  id?: string;
  unitNumber: string;
  productCode: string;
  description: string;
  testResult?: any;
  testType?: PQCTestType;
  testDate?: string | Date;
  eligibilityStatus?: string;
  isIneligible?: boolean;
  doubleBlindCheck?: boolean;
  readyToReview: boolean;
  doubleBlind: boolean;
  testStatus?: TestStatus;
  testFields?: PqcTestField[];
  inventoryId?: number;
  donationId?: number;

  [key: string]: any;

  pqcTestCriteriaId?: number;
  taskKey?: string;
  comments?: string;
  items?: PqcTestResultItem[];
}

export interface PqcTestResultItem {
  id?: number;
  key?: string;
  name?: string;
  orderNumber?: number;
  pqcTestResultId?: number;
  value?: string;
}

export class PqcTestResultEntry implements TestResultEntry {
  description: string;
  doubleBlind: boolean;
  productCode: string;
  readyToReview: boolean;
  unitNumber: string;

  [key: string]: any;
}

export interface TestResultHistory {
  modifiedBy: string;
  modifiedDate: string | Date;
  change: string;
  comment: string;
}

export interface SearchBatchNumber {
  testType?: string;
  batchNumber?: string;
}

export interface HemoglobinResultEntry {
  unitNumber: string;
  productCode: string;
  description: string;
  totalComponentHemoglobin: number;
}

export interface BatchEntry {
  id: string;
  batchNumber: string;
  testType: string;
  testDate?: string;
}

export interface NumberRange {
  min?: number;
  max?: number;
}

export type PossibleResultsType = string[] | NumberRange | null;

export interface PQCControl extends Control {
  possibleResults?: PossibleResultsType;
  possibleResultsCalculated?: PossibleResultsType;
  // TODO Remove next 2
  extraColumnHeader?: string;
  extraColumnKey?: string;
}

export interface PQCTestType {
  id: number;
  orderNumber: number;
  active?: boolean;
  descriptionKey: string;
  reviewType: string;
  maxSampleAge?: string;
  unitOfMeasure?: any;
  automaticSampling?: boolean;
  deviceTypes?: number[];
  locations?: number[];
  layout?: string;
  [key: string]: any;
}

export class TestFields implements PqcTestField {
  active: boolean;
  descriptionKey: string;
  fieldName: string;
  fieldType: string;
  id: number;
  orderNumber: number;
  testCriteriaId: number;
  testFieldOptions: TestFieldOptions[];

  [key: string]: any;

  static fromPqcTestField(pqcTestField: any) {
    const testField = new TestFields();
    testField.id = pqcTestField.id;
    testField.descriptionKey = pqcTestField.descriptionKey;
    testField.fieldName = pqcTestField.fieldName;
    testField.fieldType = pqcTestField.fieldType;
    testField.orderNumber = pqcTestField.orderNumber;
    testField.active = pqcTestField.active;
    testField.testCriteriaId = pqcTestField.testCriteriaId;
    testField.testFieldOptions = pqcTestField.testFieldOptions;
    return testField;
  }
}

export interface PqcTestField {
  id: number;
  orderNumber: number;
  active: boolean;
  descriptionKey: string;
  testCriteriaId: number;
  fieldName: string;
  fieldType: string;
  testFieldOptions?: TestFieldOptions[];
  [key: string]: any;
}

export interface TestFieldOptions {
  orderNumber: number;
  optionType: string;
  optionValue: string;
}

export interface TestValueRequired {
  name: string;
}

export interface ColumnDef {
  columnName: string;
  columnHeader?: string;
}

export interface TestReviewSearchCriteria {
  searchType?: string;
  unitOrBatchNumber?: string;
  location?: any;
  from?: string | Date;
  to?: string | Date;
  testType?: PQCTestType;
}

export interface ReviewSearchCriteria {
  searchParams?: TestReviewSearchCriteria;
  searchCriteria?: any;
}
