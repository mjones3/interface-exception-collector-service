import { AsyncPipe, NgTemplateOutlet, PercentPipe } from '@angular/common';
import { Component, OnInit, computed, signal } from '@angular/core';
import { MatProgressBar } from '@angular/material/progress-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { ProcessHeaderComponent, ProcessHeaderService } from '@shared';
import { FuseCardComponent } from '../../../../@fuse';
import { ActionButtonComponent } from '../../../shared/components/action-button/action-button.component';
import {
    ShipmentDetailResponseDTO,
    ShipmentItemResponseDTO,
} from '../models/shipment-info.dto';
import { ShipmentService } from '../services/shipment.service';
import { OrderWidgetsSidebarComponent } from '../shared/order-widgets-sidebar/order-widgets-sidebar.component';

@Component({
    selector: 'app-verify-products',
    standalone: true,
    imports: [
        AsyncPipe,
        ProcessHeaderComponent,
        ActionButtonComponent,
        FuseCardComponent,
        MatProgressBar,
        NgTemplateOutlet,
        OrderWidgetsSidebarComponent,
        PercentPipe,
    ],
    templateUrl: './verify-products.component.html',
})
export class VerifyProductsComponent implements OnInit {
    protected shipmentId: number;

    protected shipmentSignal = signal<ShipmentDetailResponseDTO>(null);
    protected productItems = computed<ShipmentItemResponseDTO[]>(() =>
        this.shipmentSignal()?.items ? [...this.shipmentSignal().items] : []
    );

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private shipmentService: ShipmentService,
        protected header: ProcessHeaderService
    ) {
        this.shipmentId = Number(this.route.snapshot.params?.id);
    }

    ngOnInit(): void {
        this.fetchShipmentDetails();
    }

    fetchShipmentDetails(): void {
        this.shipmentService
            .getShipmentById(this.shipmentId)
            .subscribe((result) => {
                this.shipmentSignal.set(result.data?.getShipmentDetailsById);
            });
    }
}
