import { AutoFocusIfDirective } from './auto-focus-if/auto-focus-if.directive';
import { BarcodeDirective } from './barcode/barcode.directive';
import { CodabarBarcodeDirective } from './codabar-barcode/codabar-barcode.directive';
import { CodabarProductBarcodeDirective } from './codabar-product-barcode/codabar-product-barcode.directive';
import { ControlErrorContainerDirective } from './control-error-container/control-error-container.directive';
import { ControlErrorsDirective } from './control-errors/control-errors.directive';
import { FormSubmitDirective } from './form-submit/form-submit.directive';
import { GroupErrorMatcherDirective } from './group-error-matcher/group-error-matcher.directive';
import { MaskRegexDirective } from './mask-regex/mask-regex.directive';
import { MinValueValidatorDirective } from './min-value-validator/min-value-validator.directive';
import { NoControlErrorsDirective } from './no-control-errors/no-control-errors.directive';
import { PermissionsOnlyDirective } from './permissions/permissions-only.directive';
import { FullProductCodeDirective } from './product-code/full-product-code.directive';

export const COMMONS_DIRECTIVES = [
  MaskRegexDirective,
  AutoFocusIfDirective,
  ControlErrorsDirective,
  ControlErrorContainerDirective,
  FormSubmitDirective,
  NoControlErrorsDirective,
  BarcodeDirective,
  CodabarBarcodeDirective,
  CodabarProductBarcodeDirective,
  FullProductCodeDirective,
  GroupErrorMatcherDirective,
  MinValueValidatorDirective,
  PermissionsOnlyDirective,
];
