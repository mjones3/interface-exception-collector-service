import { AsyncPipe, CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { ApolloError } from '@apollo/client';
import { FuseCardComponent } from '@fuse/components/card/public-api';
import {
    Description,
    ProcessHeaderComponent,
    ProcessHeaderService,
    ToastrImplService,
} from '@shared';
import { ERROR_MESSAGE } from 'app/core/data/common-labels';
import { OrderWidgetsSidebarComponent } from 'app/modules/shipments/shared/order-widgets-sidebar/order-widgets-sidebar.component';
import { ProductFamilyMap } from 'app/shared/models/product-family.model';
import { ToastrModule } from 'ngx-toastr';
import { ButtonModule } from 'primeng/button';
import { DropdownModule } from 'primeng/dropdown';
import { TableModule } from 'primeng/table';
import { finalize } from 'rxjs';
import {
    OrderDetailsDto,
    OrderItemDetailsDto,
} from '../../models/order-details.dto';
import { OrderService } from '../../services/order.service';
@Component({
    selector: 'app-order-details',
    standalone: true,
    imports: [
        CommonModule,
        TableModule,
        MatDividerModule,
        FuseCardComponent,
        AsyncPipe,
        ProcessHeaderComponent,
        ToastrModule,
        MatIconModule,
        MatButtonModule,
        MatProgressBarModule,
        OrderWidgetsSidebarComponent,
        ButtonModule,
        DropdownModule,
    ],
    templateUrl: './order-details.component.html',
    styleUrl: './order-details.component.scss',
})
export class OrderDetailsComponent implements OnInit {
    expandedRows = {};
    orderInfoDescriptions: Description[] = [];
    shippingInfoDescriptions: Description[] = [];
    billInfoDescriptions: Description[] = [];
    orderDetailsInfo: OrderDetailsDto;
    products: OrderItemDetailsDto[] = [];
    loading = true;
    protected readonly ProductFamilyMap = ProductFamilyMap;

    constructor(
        public header: ProcessHeaderService,
        private route: ActivatedRoute,
        private _router: Router,
        private orderService: OrderService,
        public toaster: ToastrImplService
    ) {}

    get labelingProductCategory() {
        return this.orderDetailsInfo
            ? this.orderDetailsInfo?.productCategory
            : '';
    }

    get orderId() {
        return this.route.snapshot.params?.id;
    }

    ngOnInit(): void {
        this.fetchOrderDetails();
    }

    fetchOrderDetails(): void {
        this.orderService
            .getOrderById(this.orderId, true)
            .pipe(finalize(() => (this.loading = false)))
            .subscribe({
                next: (result) => {
                    this.orderDetailsInfo = result.data?.findOrderById;
                    this.products =
                        this.orderDetailsInfo?.orderItems?.map((item) =>
                            this.convertItemToProduct(item)
                        ) ?? [];
                    this.updateWidgets();
                },
                error: (error: ApolloError) => {
                    this.toaster.error(ERROR_MESSAGE);
                    throw error;
                },
            });
    }

    private convertItemToProduct(
        item: OrderItemDetailsDto
    ): OrderItemDetailsDto {
        return {
            id: item.id,
            orderId: item.orderId,
            productFamily: item.productFamily,
            bloodType: item.bloodType,
            quantity: item.quantity,
            comments: item.comments,
            createDate: item.createDate,
            modificationDate: item.modificationDate,
        };
    }

    private updateWidgets() {
        this.orderInfoDescriptions = this.orderService.getOrderInfoDescriptions(
            this.orderDetailsInfo
        );
        this.shippingInfoDescriptions =
            this.orderService.getShippingInfoDescriptions(
                this.orderDetailsInfo
            );
        this.billInfoDescriptions =
            this.orderService.getBillingInfoDescriptions(this.orderDetailsInfo);
    }

    backToSearch(): void {
        this._router.navigateByUrl('/orders/search');
    }
}
