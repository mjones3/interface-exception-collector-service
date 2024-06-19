import { Component, OnInit } from '@angular/core';
import { Observable, of } from 'rxjs';
import { PackingListLabelDTO } from '@rsa/commons';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';

@Component({
  selector: 'rsa-view-shipping-label',
  templateUrl: './view-shipping-label.component.html',
})
export class ViewShippingLabelComponent implements OnInit {

  model$: Observable<Partial<PackingListLabelDTO>> = of();

  constructor(
    private domSanitizer: DomSanitizer
  ) {}

  ngOnInit(): void {
  }

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
