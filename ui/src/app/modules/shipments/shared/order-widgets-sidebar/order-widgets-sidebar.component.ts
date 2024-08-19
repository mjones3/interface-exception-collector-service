import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { MatExpansionModule } from '@angular/material/expansion';
import { ActivatedRoute, Router } from '@angular/router';
import {
    Description,
    DescriptionCardComponent,
    ProcessHeaderService,
    WidgetComponent,
} from '@shared';

@Component({
    standalone: true,
    selector: 'rsa-order-widget-sidebar',
    templateUrl: './order-widgets-sidebar.component.html',
    styleUrls: ['./order-widgets-sidebar.component.scss'],
    imports: [
        CommonModule,
        WidgetComponent,
        DescriptionCardComponent,
        MatExpansionModule,
    ],
})
export class OrderWidgetsSidebarComponent {
    @Input() comments: string;
    @Input() orderInfoDescriptions: Description[] = [];
    @Input() shippingInfoDescriptions: Description[] = [];
    @Input() billInfoDescriptions: Description[] = [];

    constructor(
        protected router: Router,
        protected activeRoute: ActivatedRoute,
        protected headerService: ProcessHeaderService
    ) {}
}
