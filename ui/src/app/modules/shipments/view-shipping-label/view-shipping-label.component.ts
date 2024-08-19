import { Component } from '@angular/core';
import { AsyncPipe, DatePipe } from '@angular/common';
import { Observable, of } from 'rxjs';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { PackingListLabelDTO } from '../models/packing-list.model';

@Component({
  selector: 'app-view-shipping-label',
  standalone: true,
    imports: [
      AsyncPipe,
      DatePipe,
    ],
  templateUrl: './view-shipping-label.component.html',
})
export class ViewShippingLabelComponent {

  model$: Observable<Partial<PackingListLabelDTO>> = of();

  constructor(
    private domSanitizer: DomSanitizer
  ) {}

  getBase64DataImage(payload: string): SafeResourceUrl {
    return this.domSanitizer.bypassSecurityTrustResourceUrl(`data:image/*;base64,${payload}`);
  }

  get navigatorLanguage() {
    return navigator.languages?.[0] ?? navigator.language;
  }

  get localTimezone() {
    const dateParts =  new Date()
      .toLocaleTimeString(this.navigatorLanguage,{ timeZoneName: 'short' })
      .split(' ');

    return dateParts?.length > 0 ? dateParts[dateParts.length - 1] : '';
  }

}
