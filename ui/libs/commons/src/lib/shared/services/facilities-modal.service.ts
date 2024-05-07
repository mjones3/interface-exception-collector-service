import { EventEmitter, Injectable, Output } from '@angular/core';

@Injectable()
export class FacilitiesModalService {
  @Output() getCurrentFacilityName: EventEmitter<any> = new EventEmitter();

  constructor() {
  }

  public setCurrentFacility(facility: string) {
    this.getCurrentFacilityName.emit(facility);
  }

}
