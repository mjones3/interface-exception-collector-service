import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { ProcessHeaderService } from '@rsa/commons';
import { TreoSplashScreenService } from '@treo';
import { filter, take } from 'rxjs/operators';

@Component({
  selector: 'rsa-empty-layout',
  templateUrl: './empty-layout.component.html',
})
export class EmptyLayoutComponent implements OnInit {
  constructor(
    protected router: Router,
    protected activeRoute: ActivatedRoute,
    protected headerService: ProcessHeaderService,
    private splash: TreoSplashScreenService
  ) {}

  ngOnInit(): void {
    // Initial subtitle load
    this.setSubtitle();
    this.router.events.pipe(filter(event => event instanceof NavigationEnd)).subscribe(() => {
      this.setSubtitle();
      this.splash.hide();
    });
  }

  setSubtitle(): void {
    // Title from Main Route
    if (this.activeRoute.data) {
      this.activeRoute.data.pipe(take(1)).subscribe(data => {
        this.headerService.setTitle(data && data.title ? data.title : '');
      });
    }

    // Title/SubTitle from Child Routes
    if (this.activeRoute.firstChild) {
      this.activeRoute.firstChild.data.pipe(take(1)).subscribe(data => {
        this.headerService.setSubtitle(data && data.subTitle ? data.subTitle : '');
        this.headerService.setMainSubTitle(data && data.mainSubTitle ? data.mainSubTitle : '');
        if (data && data.title) {
          this.headerService.title.next(data.title);
        }
      });
    }
  }
}
