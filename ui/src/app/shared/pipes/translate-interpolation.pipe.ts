import { Pipe, PipeTransform } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { interpolate } from '../utils/utils';

@Pipe({
  name: 'translateInterpolation',
})
export class TranslateInterpolationPipe implements PipeTransform {
  constructor(private translateService: TranslateService) {}

  transform(value: string, args: any): string {
    const translateValue: string = this.translateService.instant(value);
    return interpolate(translateValue, args);
  }
}
