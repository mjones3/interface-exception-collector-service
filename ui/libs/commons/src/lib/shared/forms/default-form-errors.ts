import { InjectionToken } from '@angular/core';

const defaultFormErrorsFactory = {
  required: fieldName => `${fieldName} is required`,
  pattern: fieldName => `${fieldName} is not valid`,
  requiredTrue: fieldName => `${fieldName} must be true`,
  email: fieldName => `${fieldName} should be a valid email`,
  min: (fieldName, { min, actual }) => `${fieldName} should be higher than ${min} but user entered ${actual}`,
  max: (fieldName, { max, actual }) => `${fieldName} should be lower than ${max} but user entered ${actual}`,
  minlength: (fieldName, { requiredLength, actualLength }) =>
    `${fieldName} must be at least ${requiredLength} characters long but user entered ${actualLength}`,
  maxlength: (fieldName, { requiredLength, actualLength }) =>
    `${fieldName} cannot be more than ${requiredLength} characters long but user entered ${actualLength}`,
};

export const FORM_ERRORS = new InjectionToken('FORM_ERRORS', {
  providedIn: 'root',
  factory: () => defaultFormErrorsFactory,
});
