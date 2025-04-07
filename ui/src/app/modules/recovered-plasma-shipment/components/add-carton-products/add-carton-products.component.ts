import { AsyncPipe } from '@angular/common';
import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { ProcessHeaderComponent, ProcessHeaderService } from '@shared';

@Component({
    selector: 'app-add-carton-products',
    standalone: true,
    imports: [AsyncPipe, ProcessHeaderComponent],
    templateUrl: './add-carton-products.component.html',
})
export class AddCartonProductsComponent {
    constructor(
        public header: ProcessHeaderService,
        private router: Router
    ) {}
}
