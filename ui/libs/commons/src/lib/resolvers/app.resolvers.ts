import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Resolve, RouterStateSnapshot } from '@angular/router';
import { TreoNavigationItem } from '@treo';
import { forkJoin, Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { ROLE_CREATE_ORDER_ALL } from '../core/auth/auth.constant';
import { AuthService } from '../core/auth/auth.service';
import { MenuDto } from '../shared/models/index';
import { EnvironmentConfigService, MenuService } from '../shared/services/index';

@Injectable({
  providedIn: 'root',
})
export class InitialDataResolver implements Resolve<any> {
  constructor(
    private auth: AuthService,
    private config: EnvironmentConfigService,
    private menuService: MenuService,
    private _httpClient: HttpClient
  ) {}

  // -----------------------------------------------------------------------------------------------------
  // @ Private methods
  // -----------------------------------------------------------------------------------------------------

  /**
   * Resolver
   *
   * @param route
   * @param state
   */
  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<any> {
    return forkJoin([
      // Navigation data
      this._loadNavigation(),
      // Shortcuts
      this._loadShortcuts(),
      // User
      this._loadUser(),
    ]).pipe(
      map(([menu, shortcuts, user]) => {
        const navigation = this._convertMenu(menu);
        return {
          navigation,
          shortcuts: shortcuts.shortcuts,
          user,
        };
      })
    );
  }

  /**
   * Load navigation data
   *
   * @private
   */
  private _loadNavigation(): Observable<any> {
    return this.menuService.getMenu(this.config.env.uuid);
  }

  /**
   * Convert Menu DTO to TreoNavigationItem
   *
   * @private
   */
  private _convertMenu(menus: MenuDto[]): TreoNavigationItem[] {
    const menuMap = new Map<number, TreoNavigationItem>();
    menus.forEach((menu: MenuDto) => {
      if (menu.enabled) {
        const item: TreoNavigationItem = {
          id: menu.id,
          title: menu.title,
          type: menu.type.toLowerCase(),
          icon: menu.icon,
          link: menu.link,
          classes: menu.classes,
          iconClasses: menu.iconClasses,
        };
        Object.keys(item).forEach(k => (item[k] === null || item[k] === '') && delete item[k]);
        if (menu.parentId && menuMap.get(menu.parentId)) {
          if (menuMap.get(menu.parentId).children) {
            menuMap.get(menu.parentId).children.push(item);
          } else {
            menuMap.get(menu.parentId).children = [item];
          }
        } else if (!menu.parentId) {
          menuMap.set(menu.id, item);
        }
      }
    });
    return Array.from(menuMap.values());
  }

  /**
   * Load shortcuts
   *
   * @private
   */
  private _loadShortcuts(): Observable<any> {
    return this._httpClient.get('api/common/shortcuts');
  }

  /**
   * Load user
   *
   * @private
   */
  private _loadUser(): Promise<any> {
    this.auth.hasUserRoles([ROLE_CREATE_ORDER_ALL]);
    return this.auth.loadUserProfile().then(
      user => {
        return user;
      },
      error => {
        return this.auth.login();
      }
    );
  }
}
