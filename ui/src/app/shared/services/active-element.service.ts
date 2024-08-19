import { DOCUMENT } from '@angular/common';
import { Inject, Injectable } from '@angular/core';
import { BehaviorSubject, fromEvent } from 'rxjs';
import { filter } from 'rxjs/operators';
import { isInputElement } from '../utils/utils';

@Injectable({
  providedIn: 'root'
})
export class ActiveElementService {
  private element: BehaviorSubject<HTMLElement> = new BehaviorSubject<HTMLElement>(null);
  element$ = this.element.asObservable();

  constructor(@Inject(DOCUMENT) private document: any) {
    fromEvent(document, 'focusin')
      .pipe(filter((el: any) => isInputElement(el.target as HTMLElement)))
      .subscribe((el) => this.element.next(el.target as HTMLElement));
  }

  getElement(): HTMLElement {
    return this.element.getValue();
  }

  clearElement() {
    this.element.next(null);
  }
}
