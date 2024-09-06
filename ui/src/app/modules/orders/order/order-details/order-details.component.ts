import { AsyncPipe, CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ApolloError } from '@apollo/client';
import { FuseCardComponent } from '@fuse/components/card/public-api';
import {
    Description,
    ProcessHeaderComponent,
    ProcessHeaderService,
    ToastrImplService,
} from '@shared';
import { OrderWidgetsSidebarComponent } from 'app/modules/shipments/shared/order-widgets-sidebar/order-widgets-sidebar.component';
import { ProductFamilyMap } from 'app/shared/models/product-family.model';
import { ToastrModule } from 'ngx-toastr';
import { ButtonModule } from 'primeng/button';
import { DropdownModule } from 'primeng/dropdown';
import { TableModule } from 'primeng/table';
import {
    Subscription,
    filter,
    finalize,
    map,
    of,
    switchMap,
    take,
    takeWhile,
    timer,
} from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ERROR_MESSAGE } from '../../../../core/data/common-labels';
import {
    DEFAULT_PAGE_SIZE_DIALOG_HEIGHT,
    DEFAULT_PAGE_SIZE_DIALOG_LANDSCAPE_WIDTH,
} from '../../../../core/models/browser-printing.model';
import { TagComponent } from '../../../../shared/components/tag/tag.component';
import { OrderStatusMap } from '../../../../shared/models/order-status.model';
import { PickListDTO } from '../../graphql/mutation-definitions/generate-pick-list.graphql';
import {
    Notification,
    OrderShipmentDTO,
} from '../../graphql/query-definitions/order-details.graphql';
import { OrderDetailsDTO } from '../../models/order-details.dto';
import { OrderService } from '../../services/order.service';
import { ViewPickListComponent } from '../view-pick-list/view-pick-list.component';

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
        RouterLink,
        TagComponent,
    ],
    templateUrl: './order-details.component.html',
    styleUrl: './order-details.component.scss',
})
export class OrderDetailsComponent implements OnInit, OnDestroy {
    static readonly POLLING_INTERVAL = 2 * 1000; // 2 seconds
    static readonly POLLING_MAX_TIMEOUT = 60 * 1000; // 60 seconds

    readonly ProductFamilyMap = ProductFamilyMap;

    expandedRows = {};

    orderDetails: OrderDetailsDTO;
    private notifications: Notification[] = [];

    shipments: OrderShipmentDTO[] = [];
    pollingSubscription: Subscription;
    filledOrdersCount = 0;
    totalOrderProducts: number;

    loading = true;
    loadingPickList = false;

    constructor(
        public header: ProcessHeaderService,
        private route: ActivatedRoute,
        private router: Router,
        private matDialog: MatDialog,
        private orderService: OrderService,
        private toaster: ToastrImplService
    ) {}

    get isOrderComplete(): boolean {
        return this.orderDetails?.status === 'COMPLETED';
    }

    get products() {
        return this.orderDetails?.orderItems ?? [];
    }

    get orderId() {
        return this.route.snapshot.params?.id;
    }

    ngOnInit(): void {
        this.fetchOrderDetails();
    }

    ngOnDestroy() {
        if (this.pollingSubscription && !this.pollingSubscription.closed) {
            this.pollingSubscription.unsubscribe();
        }
    }

    fetchOrderDetails(): void {
        this.orderService
            .getOrderById(this.orderId)
            .pipe(
                catchError(this.handleError),
                finalize(() => (this.loading = false)),
                map((result) => result?.data?.findOrderById),
                switchMap(({ notifications, data: orderDetails }) => {
                    this.orderDetails = orderDetails;
                    this.notifications = notifications;

                    this.totalOrderProducts = this.orderDetails.totalProducts;
                    this.filledOrdersCount = this.orderDetails.totalShipped;
                    return orderDetails.status !== 'OPEN'
                        ? this.orderService
                              .findOrderShipmentByOrderId(orderDetails.id)
                              .pipe(
                                  catchError(this.handleError),
                                  map((result) =>
                                      [
                                          result?.data
                                              ?.findOrderShipmentByOrderId,
                                      ].filter(Boolean)
                                  ) // Backend is sending only one record
                              )
                        : of([]);
                })
            )
            .subscribe((result: OrderShipmentDTO[]) => {
                this.shipments = result ?? [];
            });
    }

    viewPickList(orderId: number): void {
        this.pollingSubscription?.unsubscribe();
        this.loadingPickList = true;
        this.pollingSubscription = this.orderService
            .generatePickList(orderId)
            .pipe(
                finalize(() => (this.loadingPickList = false)),
                catchError(this.handleError),
                map((response) => response?.data?.generatePickList),
                switchMap((pickList) =>
                    this.openPickListDialog(pickList).afterOpened()
                ),

                // Polling waiting for shipment to be completed
                switchMap(() =>
                    timer(0, OrderDetailsComponent.POLLING_INTERVAL).pipe(
                        take(
                            OrderDetailsComponent.POLLING_MAX_TIMEOUT /
                                OrderDetailsComponent.POLLING_INTERVAL
                        ),
                        switchMap(() =>
                            this.orderService
                                .findOrderShipmentByOrderId(orderId)
                                .pipe(
                                    catchError(this.handleError),
                                    map(
                                        (response) =>
                                            response?.data
                                                ?.findOrderShipmentByOrderId
                                    )
                                )
                        ),
                        takeWhile((orderShipment) => !orderShipment, true)
                    )
                ),
                filter((orderShipment) => !!orderShipment),

                // Shipment created, requesting Order Details
                switchMap((orderShipment) => {
                    this.shipments = [orderShipment]; // Backend is sending only one record
                    return this.orderService.getOrderById(orderId).pipe(
                        catchError(this.handleError),
                        map((response) => response?.data?.findOrderById)
                    );
                })
            )
            // Update Order Details
            .subscribe(({ notifications, data: orderDetails }) => {
                this.orderDetails = orderDetails;
                this.notifications = notifications;
            });
    }

    private openPickListDialog(
        pickListDTO: PickListDTO
    ): MatDialogRef<ViewPickListComponent> {
        const dialogRef = this.matDialog.open(ViewPickListComponent, {
            id: 'ViewPickListDialog',
            width: DEFAULT_PAGE_SIZE_DIALOG_LANDSCAPE_WIDTH,
            height: DEFAULT_PAGE_SIZE_DIALOG_HEIGHT,
        });
        dialogRef.componentInstance.model$ = of(pickListDTO);
        return dialogRef;
    }

    get inventoryServiceIsDownNotification(): Notification {
        return this.notifications?.find(
            (n) => n.name === 'INVENTORY_SERVICE_IS_DOWN'
        );
    }

    backToSearch(): void {
        this.router.navigateByUrl('/orders/search');
    }

    handleError(error: ApolloError): never {
        if (error?.cause?.message) {
            this.toaster?.error(error?.cause?.message);
            throw error;
        }
        this.toaster?.error(ERROR_MESSAGE);
        throw error;
    }

    get orderInfoDescriptions(): Description[] {
        return [
            {
                label: 'BioPro Order Number',
                value: this.orderDetails?.orderNumber?.toString(),
            },
            {
                label: 'External order ID',
                value: this.orderDetails?.externalId,
            },
            {
                label: 'Priority',
                value: this.orderDetails?.priority,
            },
            {
                label: 'Status',
                value: this.orderDetails?.status
                    ? OrderStatusMap[this.orderDetails.status]
                    : 'Unknown',
            },
        ];
    }

    get shippingInfoDescriptions(): Description[] {
        return [
            {
                label: 'Customer Code',
                value: this.orderDetails?.shippingCustomerCode.toString(),
            },
            {
                label: 'Customer Name',
                value: this.orderDetails?.shippingCustomerName.toString(),
            },
            {
                label: 'Shipping Method',
                value: this.orderDetails?.shippingMethod.toString(),
            },
        ];
    }

    get billInfoDescriptions(): Description[] {
        return [
            {
                label: 'Billing Customer Code',
                value: this.orderDetails?.billingCustomerCode.toString(),
            },
            {
                label: 'Billing Customer Name',
                value: this.orderDetails?.billingCustomerName.toString(),
            },
        ];
    }
}
