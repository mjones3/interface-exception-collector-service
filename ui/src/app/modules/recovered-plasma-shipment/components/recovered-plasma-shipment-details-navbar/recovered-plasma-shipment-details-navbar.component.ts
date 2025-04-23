import { Component, computed, model } from '@angular/core';
import { MatTabLink, MatTabNav, MatTabNavPanel } from '@angular/material/tabs';
import { Router } from '@angular/router';

export type LinkLabel = string;
export type LinkRoute = string;

@Component({
    selector: 'biopro-recovered-plasma-shipment-details-navbar',
    standalone: true,
    imports: [MatTabLink, MatTabNav, MatTabNavPanel],
    templateUrl: './recovered-plasma-shipment-details-navbar.component.html',
})
export class RecoveredPlasmaShipmentDetailsNavbarComponent {
    links = model.required<Record<LinkLabel, LinkRoute>>();
    linkEntries = computed(() => Object.entries(this.links()) ?? []);
    activeLinkRoute = model<LinkRoute>();

    constructor(private router: Router) {}

    protected async handleNavigation(link: LinkRoute): Promise<boolean> {
        return await this.router.navigateByUrl(link);
    }
}
