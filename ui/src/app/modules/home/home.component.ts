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
    elem;

    /**
     * Constructor
     */
    constructor(private facilityService: FacilityService) {}

    ngOnInit(): void {
        this.elem = document.documentElement;
        //this.authService.signOut();
        if (!this.facilityService.checkFacilityCookie()) {
            this.facilityService
                .getFacilityDialog(FilterableDropDownComponent)
                .subscribe();
        }
    }

    /* TODO: functionality needs to be fixed to prevent an uncaught (in promise) error */
    openFullscreen() {
        if (this.elem.requestFullscreen) {
            this.elem.requestFullscreen();
        } else if (this.elem.mozRequestFullScreen) {
            /* Firefox */
            this.elem.mozRequestFullScreen();
        } else if (this.elem.webkitRequestFullscreen) {
            /* Chrome, Safari and Opera */
            this.elem.webkitRequestFullscreen();
        } else if (this.elem.msRequestFullscreen) {
            /* IE/Edge */
            this.elem.msRequestFullscreen();
        }
    }
}
