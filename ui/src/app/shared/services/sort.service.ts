import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { SortEvent } from 'primeng/api';

@Injectable({
  providedIn: 'root',
})
export class SortService {
  constructor(private translateService: TranslateService) {}

  sortByDescriptionKey(list: any[]) {
    return list.sort((a, b) =>
      this.translateService.instant(a.descriptionKey) < this.translateService.instant(b.descriptionKey) ? -1 : 1
    );
  }

  customSort(event: SortEvent) {
    event.data.sort((data1, data2) => {
      const fields = event.field.split('.');
      let value1 = null;
      let value2 = null;
      if (fields.length === 2) {
        value1 = data1[fields[0]][fields[1]];
        value2 = data2[fields[0]][fields[1]];
      } else {
        value1 = data1[fields[0]];
        value2 = data2[fields[0]];
      }

      let result = null;

      if (value1 === null && value2 != null) result = -1;
      else if (value1 != null && value2 === null) result = 1;
      else if (value1 === null && value2 === null) result = 0;
      else if (typeof value1 === 'string' && typeof value2 === 'string') {
        value1 = this.translateService.instant(value1);
        value2 = this.translateService.instant(value2);
        result = value1.localeCompare(value2);
      } else result = value1 < value2 ? -1 : value1 > value2 ? 1 : 0;
      return event.order * result;
    });
  }
}
