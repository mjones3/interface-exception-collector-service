import { Component, OnInit, ViewEncapsulation } from '@angular/core';

import { DISTRIBUTION_LOCATION_TYPE_IDS, FacilityPickerListComponent, FacilityService } from '@rsa/commons';

@Component({
  templateUrl: './home.component.html',
  encapsulation: ViewEncapsulation.None,
})
export class LandingHomeComponent implements OnInit {
  constructor(private facilityService: FacilityService) {}

  ngOnInit(): void {
    if (!this.facilityService.checkFacilityCookie()) {
      this.facilityService.getFacilityDialog(FacilityPickerListComponent, DISTRIBUTION_LOCATION_TYPE_IDS).subscribe();
    }
  }
}
