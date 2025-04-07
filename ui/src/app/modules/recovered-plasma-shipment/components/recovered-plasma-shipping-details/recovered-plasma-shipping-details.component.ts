import { AsyncPipe } from '@angular/common';
import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { ProcessHeaderComponent, ProcessHeaderService } from '@shared';
import { ActionButtonComponent } from 'app/shared/components/buttons/action-button.component';

@Component({
    selector: 'biopro-recovered-plasma-shipping-details',
    standalone: true,
    imports: [ProcessHeaderComponent, ActionButtonComponent, AsyncPipe],
    templateUrl: './recovered-plasma-shipping-details.component.html',
})
export class RecoveredPlasmaShippingDetailsComponent {
    constructor(
        public header: ProcessHeaderService,
        private router: Router
    ) {}
}
