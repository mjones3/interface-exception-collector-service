import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnDestroy, OnInit } from '@angular/core';
import { cloneDeep } from 'lodash-es';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { TreoNavigationService } from '../../../navigation.service';
import { TreoNavigationItem } from '../../../navigation.types';
import { TreoVerticalNavigationComponent } from '../../vertical.component';

@Component({
  selector: 'treo-vertical-navigation-basic-item',
  templateUrl: './basic.component.html',
  styles: [],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TreoVerticalNavigationBasicItemComponent implements OnInit, OnDestroy {
  // Item
  @Input()
  item: TreoNavigationItem;

  // Name
  @Input()
  name: string;

  // Private
  private _treoVerticalNavigationComponent: TreoVerticalNavigationComponent;
  private _unsubscribeAll: Subject<any>;

  private _defaultNavigation: TreoNavigationItem[] = [];

  /**
   * Constructor
   *
   * @param {ChangeDetectorRef} _changeDetectorRef
   * @param {TreoNavigationService} _treoNavigationService
   */
  constructor(private _changeDetectorRef: ChangeDetectorRef, private _treoNavigationService: TreoNavigationService) {
    // Set the private defaults
    this._unsubscribeAll = new Subject();
  }

  // -----------------------------------------------------------------------------------------------------
  // @ Lifecycle hooks
  // -----------------------------------------------------------------------------------------------------

  /**
   * On init
   */
  ngOnInit(): void {
    // Get the parent navigation component
    this._treoVerticalNavigationComponent = this._treoNavigationService.getComponent(this.name);

    // Subscribe to onRefreshed on the navigation component
    this._treoVerticalNavigationComponent.onRefreshed.pipe(takeUntil(this._unsubscribeAll)).subscribe(() => {
      // Mark for check
      this._changeDetectorRef.markForCheck();
    });
  }

  /**
   * On destroy
   */
  ngOnDestroy(): void {
    // Unsubscribe from all subscriptions
    this._unsubscribeAll.next();
    this._unsubscribeAll.complete();
  }

  /**
   * Reset Navigation Menu
   */
  onBackClick(): void {
    const treoNavItem = cloneDeep(this._defaultNavigation);
    this._treoVerticalNavigationComponent.navigation[0].children = [];
    this._treoVerticalNavigationComponent.navigation = treoNavItem;
    this._treoVerticalNavigationComponent.resetHomeMenuStatus();
  }

  getItemId(title) {
    return this._treoNavigationService.getItemId(title);
  }
}
