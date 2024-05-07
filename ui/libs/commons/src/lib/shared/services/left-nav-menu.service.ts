import { EventEmitter, Injectable, Output } from '@angular/core';
import { TreoNavigationService, TreoVerticalNavigationComponent } from '@treo';

@Injectable({
  providedIn: 'root',
})
export class LeftNavMenuService {
  @Output() getCurrentNavigationComponent: EventEmitter<any> = new EventEmitter();

  constructor(private _treoNavigationService: TreoNavigationService) {}

  public setCurrentNavigationComponent(treoVerticalNavigationComponent: TreoVerticalNavigationComponent) {
    this.getCurrentNavigationComponent.emit(treoVerticalNavigationComponent);
  }

  toggleLeftNavigation(): void {
    const navigation = this._treoNavigationService.getComponent('mainNavigation');
    if (navigation && navigation.opened) {
      navigation.close();
    }
  }
}
