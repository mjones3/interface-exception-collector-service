import { Component, HostListener, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { MenuService, Tab } from '@rsa/commons';
import { TreoNavigationService } from '@treo';
import { Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'rsa-tab-layout',
  templateUrl: './tab-layout.component.html',
  styleUrls: ['./tab-layout.component.scss'],
})
export class TabLayoutComponent implements OnInit, OnDestroy {
  tabLinks: Tab[] = [];
  activeLink: Tab;
  queryParams = {};
  routerEventsSub: Subscription;
  stickyNav = false;
  leftNavigationComponentOpened: boolean;
  leftNavigationSubscription: Subscription;
  readonly sidebarWidth = 280;
  readonly calcContentWidth = `calc(100% - ${this.sidebarWidth}px)`;

  constructor(
    private menuService: MenuService,
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private _treoNavigationService: TreoNavigationService
  ) {}

  @HostListener('window:scroll')
  onWindowScroll() {
    this.stickyNav = window.pageYOffset > 116;
  }

  async ngOnInit(): Promise<void> {
    const leftNavigationComponent = this._treoNavigationService.getComponent('mainNavigation');
    this.leftNavigationComponentOpened = leftNavigationComponent.opened;

    this.leftNavigationSubscription = leftNavigationComponent.openedChanged.subscribe(response => {
      this.leftNavigationComponentOpened = response;
    });

    this.queryParams = this.activatedRoute.snapshot.queryParams;
    this.tabLinks = await this.initTabs();
    this.activeLink = this.tabLinks.find(tab => this.router.url.includes(tab.url));
    this.routerEventsSub = this.router.events
      .pipe(filter(value => value instanceof NavigationEnd))
      .subscribe((value: NavigationEnd) => {
        this.activeLink = this.tabLinks.find(
          tab => value.url.includes(tab.url) || value.urlAfterRedirects.includes(tab.url)
        );
      });
  }

  async initTabs(): Promise<Tab[]> {
    if (this.activatedRoute.snapshot.data.menuId) {
      const menuId = this.activatedRoute.snapshot.data.menuId;
      const menuItems = await this.menuService.getMenu(menuId).toPromise();
      return menuItems.map(menuItem => {
        return {
          id: menuItem.id.toString(10),
          label: menuItem.title,
          url: menuItem.link,
          disabled: !menuItem.enabled,
        };
      });
    }
    return this.activatedRoute.snapshot.data.tabs;
  }

  navigateTo(link) {
    this.activeLink = link;
    this.router.navigate([link.url], { queryParams: this.queryParams });
  }

  ngOnDestroy() {
    if (this.leftNavigationSubscription) {
      this.leftNavigationSubscription.unsubscribe();
    }
    if (this.routerEventsSub) {
      this.routerEventsSub.unsubscribe();
    }
  }
}
