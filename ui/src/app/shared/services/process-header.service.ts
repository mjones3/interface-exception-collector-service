import { Injectable, TemplateRef } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class ProcessHeaderService {
  title = new BehaviorSubject<string>('');
  title$ = this.title.asObservable();

  subTitle = new BehaviorSubject<string>('');
  subTitle$ = this.subTitle.asObservable();

  mainSubTitle = new BehaviorSubject<string>('');
  mainSubTitle$ = this.mainSubTitle.asObservable();

  actions = new BehaviorSubject<TemplateRef<any>>(null);
  actions$ = this.actions.asObservable();

  setSubtitle(text: string) {
    this.subTitle.next(text);
  }

  setMainSubTitle(text: string) {
    this.mainSubTitle.next(text);
  }

  setTitle(text: string) {
    this.title.next(text);
  }

  setActions(actionTpl: TemplateRef<any>) {
    this.actions.next(actionTpl);
  }
}
