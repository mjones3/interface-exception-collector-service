import { Injectable, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { PageHistoryService, SessionStorageService } from '../../shared/services';

interface Filter {
  [key: string]: any;
}

@Injectable()
export class SessionFilterComponent implements OnDestroy {
  private readonly destroy$ = new Subject();
  private readonly allowedRoute: string;
  private readonly sessionFilterKey: string;

  constructor(
    protected readonly router: Router,
    protected readonly sessionStorage: SessionStorageService,
    protected readonly historyService: PageHistoryService,
    allowedRoute: string,
    sessionFilterKey: string
  ) {
    this.allowedRoute = allowedRoute;
    this.sessionFilterKey = sessionFilterKey;

    const currentUrl = this.router.url;
    this.historyService
      .getPreviousUrl()
      .pipe(takeUntil(this.destroy$))
      .subscribe(previousUrl => this.validateRuleToKeepStoreData(previousUrl, currentUrl));
  }

  ngOnDestroy(): void {
    this.destroy$.next(null);
    this.destroy$.complete();
  }

  private validateRuleToKeepStoreData(previousUrl: string, currentUrl: string) {
    const keepFilter = previousUrl && (previousUrl.includes(this.allowedRoute) || previousUrl.includes(currentUrl));
    if (!keepFilter) {
      this.sessionStorage.removeSession(this.sessionFilterKey);
    }
  }

  getFilter(): Filter {
    return this.sessionStorage.getJsonSession(this.sessionFilterKey);
  }

  setFilter(filter: Filter): void {
    this.sessionStorage.setJsonSession(this.sessionFilterKey, filter);
  }
}
