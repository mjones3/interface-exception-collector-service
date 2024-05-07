import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnDestroy, OnInit } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { TreoNavigationService } from '../../../navigation.service';
import { TreoNavigationItem } from '../../../navigation.types';
import { TreoVerticalNavigationComponent } from '../../vertical.component';

@Component({
  selector: 'treo-vertical-navigation-group-item',
  templateUrl: './group.component.html',
  styles: [],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TreoVerticalNavigationGroupItemComponent implements OnInit, OnDestroy {
  // Auto collapse
  @Input()
  touchable: boolean;

  // Auto collapse
  @Input()
  autoCollapse: boolean;

  // Item
  @Input()
  item: TreoNavigationItem;

  // Name
  @Input()
  name: string;

  // Private
  private _treoVerticalNavigationComponent: TreoVerticalNavigationComponent;
  private _unsubscribeAll: Subject<any>;

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

  // -----------------------------------------------------------------------------------------------------
  // @ Public methods
  // -----------------------------------------------------------------------------------------------------

  /**
   * Track by function for ngFor loops
   *
   * @param index
   * @param item
   */
  trackByFn(index: number, item: any): any {
    return item.id || index;
  }
}
