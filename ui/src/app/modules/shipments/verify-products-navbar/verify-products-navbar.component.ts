import { Component, computed, model } from '@angular/core';
import { MatTabLink, MatTabNav, MatTabNavPanel } from '@angular/material/tabs';
import { Router } from '@angular/router';

export type LinkLabel = string;
export type LinkRoute = string;

@Component({
    selector: 'biopro-verify-products-navbar',
    standalone: true,
    imports: [MatTabLink, MatTabNav, MatTabNavPanel],
    templateUrl: './verify-products-navbar.component.html',
    styleUrl: './verify-products-navbar.component.scss',
})
export class VerifyProductsNavbarComponent {
    links = model.required<Record<LinkLabel, LinkRoute>>();
    linkEntries = computed(() => Object.entries(this.links()) ?? []);
    activeLinkRoute = model<LinkRoute>();

    constructor(private router: Router) {}

    protected async handleNavigation(link: LinkRoute): Promise<boolean> {
        return await this.router.navigateByUrl(link);
    }
}
