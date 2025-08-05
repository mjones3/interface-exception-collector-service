import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { RouterLink } from '@angular/router';
import { FilterableDropDownComponent } from 'app/shared/components/filterable-drop-down/filterable-drop-down.component';
import { FacilityService } from 'app/shared/services';

@Component({
    selector: 'landing-home',
    templateUrl: './home.component.html',
    encapsulation: ViewEncapsulation.None,
    standalone: true,
    imports: [MatButtonModule, RouterLink],
})
export class LandingHomeComponent implements OnInit {
    /**
     * Constructor
     */
    constructor(private facilityService: FacilityService) {}

    ngOnInit(): void {
        //this.authService.signOut();
        if (!this.facilityService.checkFacilityCookie()) {
            this.facilityService
                .getFacilityDialog(FilterableDropDownComponent)
                .subscribe();
        }
    }
}
