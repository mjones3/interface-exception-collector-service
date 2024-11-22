import { AsyncPipe, CommonModule, formatDate } from '@angular/common';
import { Component, Inject, LOCALE_ID, ViewChild } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { Router } from '@angular/router';
import { ApolloError, ApolloQueryResult } from '@apollo/client';
import { FuseCardComponent } from '@fuse/components/card/public-api';
import {
    Column,
    FacilityService,
    ProcessHeaderComponent,
    ProcessHeaderService,
} from '@shared';
import { OrderStatusMap } from 'app/shared/models/order-status.model';
import { CookieService } from 'ngx-cookie-service';
import { ToastrService } from 'ngx-toastr';
import { Table, TableLazyLoadEvent, TableModule } from 'primeng/table';
import { BehaviorSubject, Subject, finalize } from 'rxjs';
import { OrderPriorityMap } from '../../../../shared/models/order-priority.model';
import { Cookie } from '../../../../shared/types/cookie.enum';
import { SearchOrderFilterDTO } from '../../models/order.dto';
import {
    OrderQueryCommandDTO,
    OrderReportDTO,
} from '../../models/search-order.model';
import { OrderService } from '../../services/order.service';
import { SearchOrderFilterComponent } from './search-filter/search-order-filter.component';

@Component({
    selector: 'app-search-orders',
    standalone: true,
    imports: [
        CommonModule,
        TableModule,
        MatDividerModule,
        FuseCardComponent,
        AsyncPipe,
        ProcessHeaderComponent,
        MatButtonModule,
        SearchOrderFilterComponent,
    ],
    templateUrl: './search-orders.component.html',
    styleUrls: ['./search-orders.component.scss'],
})
export class SearchOrdersComponent {
    protected readonly OrderStatusMap = OrderStatusMap;

    readonly hiddenColumns: Column[] = [
        {
            field: 'shippingCustomerCode',
            header: 'Ship to Customer Code',
            sortable: false,
            hidden: true,
        },
        {
            field: 'billingCustomerName',
            header: 'Bill to Customer Name',
            sortable: false,
            hidden: true,
        },
        {
            field: 'billingCustomerCode',
            header: 'Bill to Customer Code',
            sortable: false,
            hidden: true,
        },
    ];
    readonly columns: Column[] = [
        {
            field: 'orderNumber',
            header: 'BioPro Order ID',
            sortable: false,
            default: true,
        },
        {
            field: 'externalId',
            header: 'External Order ID',
            sortable: false,
            default: true,
        },
        {
            field: 'priority',
            header: 'Priority',
            templateRef: 'priorityTpl',
            sortable: false,
            sortFieldName: 'priority',
            default: true,
        },
        {
            field: 'orderStatus',
            header: 'Status',
            templateRef: 'statusTpl',
            sortable: false,
            sortFieldName: 'status',
            default: true,
        },
        {
            field: 'orderCustomerReport.name',
            header: 'Ship to Customer Name',
            templateRef: 'shipToCustomerTpl',
            sortable: false,
            default: true,
        },
        {
            field: 'createDate',
            header: 'Create Date and Time',
            sortable: false,
            templateRef: 'dateTimeTpl',
            sortFieldName: 'createDate',
            default: true,
        },
        {
            field: 'desireShipDate',
            header: 'Desired Ship Date',
            templateRef: 'dateTpl',
            sortable: false,
            default: true,
        },
        ...this.hiddenColumns,
        {
            field: '',
            header: 'Actions',
            templateRef: 'actionTpl',
            hideHeader: true,
            default: true,
        },
    ];
    isFilterToggled = false;
    defaultRowsPerPage = 20;
    defaultSortField = 'createDate';
    totalRecords = 0;
    currentFilter: SearchOrderFilterDTO;
    items$: Subject<OrderReportDTO[]> = new BehaviorSubject([]);
    loading = true;
    noResultsMessage: string;
    footerMessage = '';

    @ViewChild('orderTable', { static: false }) orderTable: Table;

    private _selectedColumns: Column[] = this.columns.filter(
        (col) => !col.hidden
    );

    constructor(
        public header: ProcessHeaderService,
        private facilityService: FacilityService,
        private orderService: OrderService,
        private router: Router,
        private toaster: ToastrService,
        private cookieService: CookieService,
        @Inject(LOCALE_ID) public locale: string
    ) {}

    toggleFilter(toggleFlag: boolean): void {
        this.isFilterToggled = toggleFlag;
    }

    private getCriteria(): OrderQueryCommandDTO {
        const criteria: OrderQueryCommandDTO = {
            locationCode: this.cookieService.get(Cookie.XFacility),
        };
        if (this.currentFilter) {
            if (this.currentFilter.orderNumber !== '') {
                criteria.orderUniqueIdentifier = this.currentFilter.orderNumber;
            }
            if (
                this.currentFilter.orderStatus &&
                this.currentFilter.orderStatus.length > 0
            ) {
                criteria.orderStatus = this.currentFilter.orderStatus;
            }
            if (
                this.currentFilter.deliveryTypes &&
                this.currentFilter.deliveryTypes.length > 0
            ) {
                criteria.deliveryTypes = this.currentFilter.deliveryTypes;
            }
            if (
                this.currentFilter.customers &&
                this.currentFilter.customers.length > 0
            ) {
                criteria.customers = this.currentFilter.customers;
            }
            if (
                this.currentFilter.createDate?.start != null &&
                this.currentFilter.createDate?.end != null
            ) {
                criteria.createDateFrom = formatDate(
                    this.currentFilter.createDate?.start,
                    'yyyy-MM-dd',
                    this.locale
                );

                criteria.createDateTo = formatDate(
                    this.currentFilter.createDate?.end,
                    'yyyy-MM-dd',
                    this.locale
                );
            }
            if (
                this.currentFilter.desiredShipDate?.start != null &&
                this.currentFilter.desiredShipDate?.end != null
            ) {
                criteria.desireShipDateFrom = formatDate(
                    this.currentFilter.desiredShipDate?.start,
                    'yyyy-MM-dd',
                    this.locale
                );

                criteria.desireShipDateTo = formatDate(
                    this.currentFilter.desiredShipDate?.end,
                    'yyyy-MM-dd',
                    this.locale
                );
            }
        }

        return criteria;
    }

    private searchOrder(event?: TableLazyLoadEvent) {
        this.footerMessage = '';
        this.loading = true;
        this.orderService
            .searchOrders(this.getCriteria())
            .pipe(finalize(() => (this.loading = false)))
            .subscribe({
                next: (response) => this.doOnSuccess(response, event),
                error: (e: ApolloError) => {
                    if (this.orderTable != null) {
                        this.orderTable.sortField = this.defaultSortField;
                    }
                    this.items$.next([]);
                    if (e?.cause?.message) {
                        this.noResultsMessage = e?.cause?.message;
                        this.toaster.warning(e?.cause?.message);
                        this.footerMessage = e?.cause?.message;
                        return;
                    }
                    this.toaster.error('Something went wrong.');
                    this.loading = false;
                    throw e;
                },
            });
    }

    private doOnSuccess(
        response: ApolloQueryResult<{ searchOrders: OrderReportDTO[] }>,
        event?: TableLazyLoadEvent
    ): void {
        if (response.data.searchOrders.length === 1 && this.isFilterApplied()) {
            this.details(response.data.searchOrders[0].orderId);
        }
        this.items$.next(response.data.searchOrders ?? []);
        if (event) {
            this.orderTable.sortField =
                typeof event.sortField === 'string'
                    ? event.sortField
                    : event.sortField?.[0] ?? this.defaultSortField;
        }
    }

    applyFilterSearch(searchCriteria: SearchOrderFilterDTO = {}): void {
        this.isFilterToggled = false;
        this.currentFilter = searchCriteria;
        this.searchOrder();
    }

    isFilterApplied() {
        return this.getCriteria()?.orderUniqueIdentifier != null;
    }

    fetchOrders(event: TableLazyLoadEvent) {
        this.searchOrder(event);
    }

    details(id: number) {
        this.router.navigateByUrl(`/orders/${id}/order-details`);
    }

    get selectedColumns(): Column[] {
        return this._selectedColumns;
    }

    set selectedColumns(val: Column[]) {
        this._selectedColumns = this.columns.filter((col) => val.includes(col));
    }

    protected readonly OrderPriorityMap = OrderPriorityMap;
}
