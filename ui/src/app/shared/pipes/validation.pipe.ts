import { Pipe, PipeTransform } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

export enum ValidationType {
  REQUIRED = 'required',
  INVALID = 'invalid',
  ALREADY_EXISTS = 'alreadyExists',
}

@Pipe({
  standalone: true,
  name: 'validation',
})
export class ValidationPipe implements PipeTransform {
  readonly validationsKeys = {
    required: 'Required',
    invalid: 'Invalid',
    alreadyExists: 'Already Exists',
  };

  constructor(private translateService: TranslateService) {}

  transform(labelKey: string, validationType: ValidationType): string {
    return `${this.translateService.instant(labelKey)} ${this.translateService.instant(
      this.validationsKeys[validationType]
    )}`;
  }
}
