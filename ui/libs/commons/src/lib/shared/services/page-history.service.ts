import { Injectable } from '@angular/core';
import { NavigationEnd, Router, RouterEvent } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { filter } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class PageHistoryService {
  currentUrl: string;
  previousUrl: BehaviorSubject<string>;

  constructor(private router: Router) {
    this.currentUrl = this.router.url;
    this.previousUrl = new BehaviorSubject(null);

    this.router.events
      .pipe(filter((event: RouterEvent) => event instanceof NavigationEnd))
      .subscribe((event: NavigationEnd) => {
        this.previousUrl.next(this.currentUrl);
        this.currentUrl = event.urlAfterRedirects;
      });
  }

  public getPreviousUrl() {
    return this.previousUrl;
  }
}
