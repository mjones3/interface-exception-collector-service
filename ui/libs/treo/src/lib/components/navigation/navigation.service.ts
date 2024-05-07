import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { TreoNavigationItem } from './navigation.types';

@Injectable({
  providedIn: 'root',
})
export class TreoNavigationService {
  // Private
  private _componentRegistry: Map<string, any>;
  private _navigationStore: Map<string, TreoNavigationItem[]>;
  private _navigationMenu: BehaviorSubject<TreoNavigationItem[] | null>;

  /**
   * Constructor
   */
  constructor(private _httpClient: HttpClient) {
    // Set the private defaults
    this._componentRegistry = new Map<string, any>();
    this._navigationStore = new Map<string, any>();
    this._navigationMenu = new BehaviorSubject(null);
  }

  // -----------------------------------------------------------------------------------------------------
  // @ Public methods
  // -----------------------------------------------------------------------------------------------------

  /**
   * Register navigation component
   *
   * @param name
   * @param component
   */
  registerComponent(name: string, component: any): void {
    this._componentRegistry.set(name, component);
  }

  /**
   * Deregister navigation component
   *
   * @param name
   */
  deregisterComponent(name: string): void {
    this._componentRegistry.delete(name);
  }

  /**
   * Get navigation component from the registry
   *
   * @param name
   */
  getComponent(name: string): any {
    return this._componentRegistry.get(name);
  }

  /**
   * Store the given navigation with the given key
   *
   * @param key
   * @param navigation
   */
  storeNavigation(key: string, navigation: TreoNavigationItem[]): void {
    // Add to the store
    this._navigationStore.set(key, navigation);
  }

  /**
   * Get navigation from storage by key
   *
   * @param key
   * @returns {any}
   */
  getNavigation(key: string): TreoNavigationItem[] {
    return this._navigationStore.get(key);
  }

  /**
   * Delete the navigation from the storage
   *
   * @param key
   */
  deleteNavigation(key: string): void {
    // Check if the navigation exists
    if (!this._navigationStore.has(key)) {
      console.warn(`Navigation with the key '${key}' does not exist in the store.`);
    }

    // Delete from the storage
    this._navigationStore.delete(key);
  }

  /**
   * Utility function that returns a flattened
   * version of the given navigation array
   *
   * @param navigation
   * @param flatNavigation
   * @returns {TreoNavigationItem[]}
   */
  getFlatNavigation(navigation: TreoNavigationItem[], flatNavigation: TreoNavigationItem[] = []): TreoNavigationItem[] {
    for (const item of navigation) {
      if (item.type === 'basic') {
        flatNavigation.push(item);
        continue;
      }

      if (
        item.type === 'aside' ||
        item.type === 'collapsable' ||
        item.type === 'group' ||
        item.type === 'COLLAPSABLE'
      ) {
        if (item.children) {
          this.getFlatNavigation(item.children, flatNavigation);
        }
      }
    }

    return flatNavigation;
  }

  /**
   * Utility function that returns the item
   * with the given id from given navigation
   *
   * @param id
   * @param navigation
   */
  getItem(id: number, navigation: TreoNavigationItem[]): TreoNavigationItem | null {
    for (const item of navigation) {
      if (item.id === id) {
        return item;
      }

      if (item.children) {
        const childItem = this.getItem(id, item.children);

        if (childItem) {
          return childItem;
        }
      }
    }

    return null;
  }

  /**
   * Utility function that returns the item's parent
   * with the given id from given navigation
   *
   * @param id
   * @param navigation
   * @param parent
   */
  getItemParent(
    id: number,
    navigation: TreoNavigationItem[],
    parent: TreoNavigationItem[] | TreoNavigationItem
  ): TreoNavigationItem[] | TreoNavigationItem | null {
    for (const item of navigation) {
      if (item.id === id) {
        return parent;
      }

      if (item.children) {
        const childItem = this.getItemParent(id, item.children, item);

        if (childItem) {
          return childItem;
        }
      }
    }

    return null;
  }

  getItemId(title) {
    const id = title
      .toLowerCase()
      .replace(/\s(.)/g, function ($1) {
        return $1.toUpperCase();
      })
      .replace(/\s/g, '')
      .replace(/^(.)/, function ($1) {
        return $1.toLowerCase();
      });
    return id + 'Menu';
  }

  get navigationMenu$(): Observable<TreoNavigationItem[]> {
    return this._navigationMenu.asObservable();
  }

  changeNavigationMenu(navigationMenu: TreoNavigationItem[]): Observable<TreoNavigationItem[]> {
    // Load the shortcuts
    this._navigationMenu.next(navigationMenu);

    // Return the shortcuts
    return this.navigationMenu$;
  }

  loadNavigation(menuTitle): Observable<any> {
    return this._httpClient.get('api/common/navigation');
  }
}
