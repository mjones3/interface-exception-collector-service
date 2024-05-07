import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { Description } from '../models/description.model';

/**
 * Service to handle description items in shared calculate volume and sterile connection embedded components
 */
@Injectable({
  providedIn: 'root',
})
export class InformationPanelService {
  private descriptions = new BehaviorSubject<Description[]>([]);
  private title = 'donation-information.label' ;
  descriptions$ = this.descriptions.asObservable();

  constructor(private translateService: TranslateService) {}

  setDescriptions(...descriptions: Description[]) {
    descriptions.forEach((element, i) => {
      const isMultipleLabels = this.checkMultipleLabels(element);
      if (isMultipleLabels) {
        const translateArray = JSON.stringify(element.value)
          .split(',')
          .map(res => {
            return this.translateService.instant(res.replace('"', ''));
          });
        descriptions[i]['value'] = '';
        descriptions[i]['value'] = translateArray.join(', ');
      }
    });
    this.descriptions.next([...descriptions]);
  }

  checkMultipleLabels(element) {
    return (
      element.value &&
      typeof element.value === 'string' &&
      element.value.indexOf(',') !== -1 &&
      element.value.indexOf('label') !== -1
    );
  }

  appendDescriptions(...descriptions: Description[]) {
    this.descriptions.next([...this.getCurrentDescriptions(), ...descriptions]);
  }

  getCurrentDescriptions(): Description[] {
    return this.descriptions.getValue();
  }

  setTitle(label: string) {
    this.title = label;
  }

  getTitle() {
    return this.translateService.instant(this.title.replace('"', ''));
  }

}
