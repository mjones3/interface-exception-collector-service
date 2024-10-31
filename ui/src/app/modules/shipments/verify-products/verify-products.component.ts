import { AsyncPipe } from '@angular/common';
import { Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ProcessHeaderComponent, ProcessHeaderService } from '@shared';
import { ActionButtonComponent } from '../../../shared/components/action-button/action-button.component';

@Component({
    selector: 'app-verify-products',
    standalone: true,
    imports: [AsyncPipe, ProcessHeaderComponent, ActionButtonComponent],
    templateUrl: './verify-products.component.html',
})
export class VerifyProductsComponent {
    constructor(
        private route: ActivatedRoute,
        private router: Router,
        protected header: ProcessHeaderService
    ) {}
}
