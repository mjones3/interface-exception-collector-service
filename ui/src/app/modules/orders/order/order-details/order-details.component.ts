import { AsyncPipe, CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FuseCardComponent } from '@fuse/components/card/public-api';
import { Store } from '@ngrx/store';
import {
    ProcessHeaderComponent,
    ProcessHeaderService,
    ToastrImplService,
} from '@shared';
import { OrderWidgetsSidebarComponent } from 'app/modules/shipments/shared/order-widgets-sidebar/order-widgets-sidebar.component';
import { ProgressBarComponent } from 'app/progress-bar/progress-bar.component';
import { OrderStatusMap } from 'app/shared/models/order-status.model';
import { ProductFamilyMap } from 'app/shared/models/product-family.model';
import { ProductIconsService } from 'app/shared/services/product-icon.service';
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
    tap,
    timer,
} from 'rxjs';
import { catchError } from 'rxjs/operators';
import { FuseConfirmationService } from '../../../../../@fuse/services/confirmation';
import { FuseConfirmationDialogComponent } from '../../../../../@fuse/services/confirmation/dialog/dialog.component';
import {
    DEFAULT_PAGE_SIZE_DIALOG_HEIGHT,
    DEFAULT_PAGE_SIZE_DIALOG_LANDSCAPE_WIDTH,
} from '../../../../core/models/browser-printing.model';
import { getAuthState } from '../../../../core/state/auth/auth.selectors';
import { ActionButtonComponent } from '../../../../shared/components/buttons/action-button.component';
import { TagComponent } from '../../../../shared/components/tag/tag.component';
import { ProductCategoryMap } from '../../../../shared/models/product-category.model';
import handleApolloError from '../../../../shared/utils/apollo-error-handling';
import { consumeNotificationMessages } from '../../../../shared/utils/notification.handling';
import { CompleteOrderComponent } from '../../complete-order/complete-order.component';
import { CompleteOrderCommandDTO } from '../../graphql/mutation-definitions/complete-order.graphql';
import { PickListDTO } from '../../graphql/mutation-definitions/generate-pick-list.graphql';
import { OrderShipmentDTO } from '../../graphql/query-definitions/order-details.graphql';
import { Notification } from '../../models/notification.dto';
import { OrderDetailsDTO } from '../../models/order-details.dto';
import { OrderService } from '../../services/order.service';
import {
    ViewPickListComponent,
    ViewPickListData,
} from '../view-pick-list/view-pick-list.component';

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
        ProgressBarComponent,
        ActionButtonComponent,
    ],
    templateUrl: './order-details.component.html',
})
export class OrderDetailsComponent implements OnInit, OnDestroy {
    protected readonly OrderStatusMap = OrderStatusMap;
    static readonly INVENTORY_SERVICE_IS_DOWN = 'INVENTORY_SERVICE_IS_DOWN';
    static readonly POLLING_INTERVAL = 2 * 1000; // 2 seconds
    static readonly POLLING_MAX_TIMEOUT = 60 * 1000; // 60 seconds

    readonly ProductFamilyMap = ProductFamilyMap;

    expandedRows = {};
    isBackOrderCreationActive: boolean;

    orderDetails: OrderDetailsDTO;
    private notifications: Notification[] = [];

    shipments: OrderShipmentDTO[] = [];
    pollingSubscription: Subscription;
    filledOrdersCount = 0;
    totalOrderProducts: number;
    loggedUserId: string;

    loading = true;
    loadingPickList = false;

    constructor(
        public header: ProcessHeaderService,
        private route: ActivatedRoute,
        private router: Router,
        private matDialog: MatDialog,
        private orderService: OrderService,
        private toaster: ToastrImplService,
        private fuseConfirmationService: FuseConfirmationService,
        private productIconService: ProductIconsService,
        private store: Store
    ) {
        this.store
            .select(getAuthState)
            .pipe(take(1))
            .subscribe((auth) => {
                this.loggedUserId = auth['id'];
            });
    }

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
                catchError((e) => handleApolloError(this.toaster, e)),
                finalize(() => (this.loading = false)),
                map((result) => result?.data?.findOrderById),
                switchMap(({ notifications, data: orderDetails }) => {
                    this.orderDetails = orderDetails;
                    this.notifications = notifications;

                    this.totalOrderProducts = this.orderDetails.totalProducts;
                    this.filledOrdersCount = this.orderDetails.totalShipped;
                    this.isBackOrderCreationActive =
                        this.orderDetails.backOrderCreationActive;
                    return orderDetails.status !== 'OPEN'
                        ? this.orderService
                              .findOrderShipmentByOrderId(orderDetails.id)
                              .pipe(
                                  catchError((e) =>
                                      handleApolloError(this.toaster, e)
                                  ),
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

    viewPickList(orderId: number, skipInventoryUnavailable = false): void {
        this.pollingSubscription?.unsubscribe();
        this.loadingPickList = true;
        this.pollingSubscription = this.orderService
            .generatePickList(orderId, skipInventoryUnavailable)
            .pipe(
                finalize(() => (this.loadingPickList = false)),
                catchError((e) => handleApolloError(this.toaster, e)),
                map((response) => response?.data?.generatePickList),
                switchMap(({ notifications, data: pickList }) => {
                    const inventoryServiceIsDown =
                        this.getInventoryServiceIsDownNotification(
                            notifications
                        );
                    if (inventoryServiceIsDown) {
                        this.openSkipInventoryUnavailableDialog(
                            inventoryServiceIsDown
                        )
                            .afterClosed()
                            .pipe(filter((value) => 'confirmed' === value)) // FuseConfirmationDialogComponent dismiss result
                            .subscribe(() => this.viewPickList(orderId, true));
                        return of(null); // do not continue
                    }
                    return of(pickList);
                }),
                filter(Boolean),
                switchMap((pickList) =>
                    this.openPickListDialog(
                        pickList,
                        skipInventoryUnavailable
                    ).afterOpened()
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
                                    catchError((e) =>
                                        handleApolloError(this.toaster, e)
                                    ),
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
                        catchError((e) => handleApolloError(this.toaster, e)),
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
        pickListDTO: PickListDTO,
        skipInventoryUnavailable: boolean
    ): MatDialogRef<ViewPickListComponent> {
        return this.matDialog.open<ViewPickListComponent, ViewPickListData>(
            ViewPickListComponent,
            {
                id: 'ViewPickListDialog',
                width: DEFAULT_PAGE_SIZE_DIALOG_LANDSCAPE_WIDTH,
                height: DEFAULT_PAGE_SIZE_DIALOG_HEIGHT,
                data: { pickListDTO, skipInventoryUnavailable },
            }
        );
    }

    private openSkipInventoryUnavailableDialog(
        notification: Notification
    ): MatDialogRef<FuseConfirmationDialogComponent> {
        return this.fuseConfirmationService.open({
            title: 'Process Message',
            message: `${notification.notificationMessage} Would you like to continue?`,
            dismissible: false,
            icon: {
                show: false,
            },
            actions: {
                confirm: {
                    label: 'Continue',
                    class: 'bg-green-highlighted text-white font-bold',
                },
                cancel: {
                    class: 'font-bold',
                },
            },
        });
    }

    getInventoryServiceIsDownNotification(
        notifications: Notification[]
    ): Notification {
        return notifications?.find(
            (n) => n.name === OrderDetailsComponent.INVENTORY_SERVICE_IS_DOWN
        );
    }

    get inventoryServiceIsDownNotification(): Notification {
        return this.getInventoryServiceIsDownNotification(this.notifications);
    }

    backToSearch(): void {
        this.router.navigateByUrl('/orders/search');
    }

    getIcon(productFamily: string) {
        return this.productIconService.getIconByProductFamily(productFamily);
    }

    openCompleteOrderDialog(): void {
        this.matDialog
            .open<
                CompleteOrderComponent,
                { isBackOrderCreationActive: boolean },
                Partial<CompleteOrderCommandDTO>
            >(CompleteOrderComponent, {
                data: {
                    isBackOrderCreationActive: this.isBackOrderCreationActive,
                },
                disableClose: true,
                id: 'CompleteOrderDialog',
                width: '30rem',
            })
            .afterClosed()
            .pipe(filter(Boolean))
            .subscribe((command) => this.complete(command));
    }

    complete(command: Partial<CompleteOrderCommandDTO>): void {
        this.orderService
            .completeOrder({
                orderId: Number(this.orderId),
                employeeId: this.loggedUserId,
                createBackOrder: false,
                ...command,
            })
            .pipe(
                tap((response) =>
                    consumeNotificationMessages(
                        this.toaster,
                        response?.data?.completeOrder?.notifications
                    )
                ),
                catchError((e) => handleApolloError(this.toaster, e))
            )
            .subscribe((response) => {
                this.orderDetails = response?.data?.completeOrder?.data;
                this.notifications =
                    response?.data?.completeOrder?.notifications ?? [];
            });
    }

    protected readonly ProductCategoryMap = ProductCategoryMap;
}
