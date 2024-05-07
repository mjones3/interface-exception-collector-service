import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { Observable, Subscription } from 'rxjs';
import { filter, take } from 'rxjs/operators';
import { ProcessHeaderService } from '../../shared/services';

export class BaseProcessComponent {
  title$: Observable<string>;
  subTitle$: Observable<string>;
  mainSubTitle$: Observable<string>;

  routeSubs: Subscription;

  constructor(
    protected router: Router,
    protected activeRoute: ActivatedRoute,
    protected headerService: ProcessHeaderService
  ) {
    this.title$ = this.headerService.title.asObservable();
    this.subTitle$ = this.headerService.subTitle.asObservable();
    this.mainSubTitle$ = this.headerService.mainSubTitle.asObservable();
  }

  init(): void {
    // Initial subtitle load
    this.setSubtitle();
    this.routeSubs = this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(event => this.setSubtitle());
  }

  setSubtitle() {
    // Title from Main Route
    if (this.activeRoute.data) {
      this.activeRoute.data
        .pipe(take(1))
        .subscribe(data => this.headerService.setTitle(data && data.title ? data.title : ''));
    }

    // Title/SubTitle from Child Routes
    if (this.activeRoute.firstChild) {
      this.activeRoute.firstChild.data.pipe(take(1)).subscribe(data => {
        this.headerService.setSubtitle(data && data.subTitle ? data.subTitle : '');
        this.headerService.setMainSubTitle(data && data.mainSubTitle ? data.mainSubTitle : '');
        if (data.title) {
          this.headerService.title.next(data.title);
        }
      });

      // When using a process embedded in other process use Page Title from 'embeddedInProcess' route parameter
      if (this.activeRoute.firstChild.snapshot.params['embeddedInProcess']) {
        this.headerService.title.next(this.activeRoute.firstChild.snapshot.params['embeddedInProcess']);
      }
    }
  }
}
