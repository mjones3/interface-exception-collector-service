import { formatDate } from '@angular/common';
import { Inject, Injectable, LOCALE_ID } from '@angular/core';
import { BehaviorSubject, of } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { DescriptionData } from '../models/description-data.model';
import { Description, ProcessPageDto } from '../models/index';
import { ProcessPageService } from './process-page.service';

@Injectable({
  providedIn: 'root'
})
export class DescriptionFactoryService {
  private currentProcess: BehaviorSubject<any> = new BehaviorSubject<any>(null);
  currentProcess$ = this.currentProcess.asObservable();

  constructor(private processPageService: ProcessPageService, @Inject(LOCALE_ID) public locale: string) {
  }

  getDescriptionsByProcess(process: string, data: DescriptionData): void {
    const processValue = this.currentProcess.getValue();
    if (!processValue || processValue !== process) {
      this.processPageService.getProcessByDescriptionBy(process)
        .pipe(
          switchMap((processPage) => {
            return of(this.getDescription(processPage.body, data));
          }));
    }
  }

  private getDescription(processPage: ProcessPageDto[], data: DescriptionData): Description[] {
    return processPage.map(processPageData => this.getDescriptionByKey(processPageData.descriptionKey, data));
  }

  getDescriptionByKey(key: string, data: DescriptionData): Description {
    let description = { label: key } as Description;
    switch (key) {
      case 'unit-number.label':
        description = { ...description, value: data.inventory.unitNumber?.toUpperCase() };
        break;
      case 'collection-date.label':
        description = { ...description, value: formatDate(data.draw.createDate, 'MMMM dd, yyyy', this.locale) };
        break;
      case 'draw-time.label':
        description = { ...description, value: formatDate(data.draw.createDate, 'HH:MM', 'en-US') };
        break;
      case 'donor-intention.label':
        description = { ...description, value: 'TODO UPDATE' }; // TODO CHECK
        break;
      case 'blood-type.label':
        description = { ...description, value: data.donor.description }; // TODO CHECK
        break;
      case 'bag-type.label':
        description = { ...description, value: data.draw.bagTypeKey }; // TODO CHECK
        break;
      case 'bag-volume.label':
        description = { ...description, value: 'TODO UPDATE' }; // TODO CHECK
        break;
    }
    return description;
  }
}
