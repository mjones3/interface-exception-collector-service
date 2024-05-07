import {registerLocaleData} from '@angular/common';
import ngEn from '@angular/common/locales/en';
import ngEs from '@angular/common/locales/es';
import {Injectable} from '@angular/core';
import {DateAdapter} from '@angular/material/core';
import {TranslateService} from '@ngx-translate/core';
import {BehaviorSubject, Observable} from 'rxjs';
import {filter} from 'rxjs/operators';

interface LangData {
  text: string;
  ng: any;
  abbr: string;
}

const DEFAULT = 'en';
const LANGS: { [key: string]: LangData } = {
  'en': {
    text: 'English',
    ng: ngEn,
    abbr: 'US',
  },
  'es': {
    text: 'Español',
    ng: ngEs,
    abbr: 'ES',
  },
};

@Injectable({
  providedIn: 'root'
})
export class I18nService {
  private change$ = new BehaviorSubject<string | null>(null);
  private _default = DEFAULT;
  // TODO load langs from DB
  private _languages = Object.keys(LANGS).map((code) => {
    const item = LANGS[code];
    return {code, text: item.text, abbr: item.abbr};
  });

  constructor(private translate: TranslateService, private adapter: DateAdapter<any>) {
    // Know in advance which languages ​​are supported
    const langs = this._languages.map((item) => item.code);
    translate.addLangs(langs);
  }

  get change(): Observable<string> {
    return this.change$.asObservable().pipe(filter((w) => w != null)) as Observable<string>;
  }

  /**
   * Change language
   * @param lang Language
   */
  use(lang: string): void {
    lang = lang || this.translate.getDefaultLang();
    if (this.currentLang === lang) {
      return;
    }
    this.updateLangData(lang);
    this.adapter.setLocale(lang);
    this.translate.use(lang).subscribe(() => this.change$.next(lang));
  }

  private updateLangData(lang: string) {
    const item = LANGS[lang];
    registerLocaleData(item.ng);
  }

  /**
   * Get current lang
   */
  get currentLang() {
    return this.translate.currentLang || this.translate.getDefaultLang() || this._default;
  }

  /**
   * Get a list of languages
   */
  getLanguages() {
    return this._languages;
  }

  /**
   * Get default lang
   */
  get defaultLang() {
    return this._default;
  }
}
