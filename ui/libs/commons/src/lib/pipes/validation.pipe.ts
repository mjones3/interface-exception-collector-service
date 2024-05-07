import { Pipe, PipeTransform } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

export enum ValidationType {
  REQUIRED = 'required',
  INVALID = 'invalid',
  ALREADY_EXISTS = 'alreadyExists',
}

@Pipe({
  name: 'validation',
})
export class ValidationPipe implements PipeTransform {
  readonly validationsKeys = {
    required: 'required.validation',
    invalid: 'invalid.validation',
    alreadyExists: 'already-exists.validation',
  };

  constructor(private translateService: TranslateService) {}

  transform(labelKey: string, validationType: ValidationType): string {
    return `${this.translateService.instant(labelKey)} ${this.translateService.instant(
      this.validationsKeys[validationType]
    )}`;
  }
}
