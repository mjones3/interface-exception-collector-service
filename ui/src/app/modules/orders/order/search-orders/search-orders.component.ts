import { AsyncPipe, CommonModule } from '@angular/common';
import { Component, ViewChild } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { Router } from '@angular/router';
import { ApolloError } from '@apollo/client';
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
import { Cookie } from '../../../../shared/types/cookie.enum';
import { OrderReportDTO } from '../../models/search-order.model';
import { OrderService } from '../../services/order.service';

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

    defaultRowsPerPage = 20;
    defaultSortField = 'createDate';
    totalRecords = 0;
    items$: Subject<OrderReportDTO[]> = new BehaviorSubject([]);
    loading = true;

    @ViewChild('orderTable', { static: true }) orderTable: Table;

    private _selectedColumns: Column[] = this.columns.filter(
        (col) => !col.hidden
    );

    constructor(
        public header: ProcessHeaderService,
        private facilityService: FacilityService,
        private orderService: OrderService,
        private router: Router,
        private toaster: ToastrService,
        private cookieService: CookieService
    ) {}

    fetchOrders(event: TableLazyLoadEvent) {
        const facilityCode = this.cookieService.get(Cookie.XFacility);
        this.orderService
            .searchOrders({ locationCode: facilityCode })
            .pipe(finalize(() => (this.loading = false)))
            .subscribe({
                next: (response) => {
                    this.orderTable.sortField =
                        typeof event.sortField === 'string'
                            ? event.sortField
                            : event.sortField?.[0] ?? this.defaultSortField;
                    this.items$.next(response.data.searchOrders ?? []);
                },
                error: (e: ApolloError) => {
                    this.orderTable.sortField = this.defaultSortField;
                    this.items$.next([]);
                    if (e?.cause?.message) {
                        this.toaster.warning(e?.cause?.message);
                        return;
                    }
                    this.toaster.error('Something went wrong.');
                    throw e;
                },
            });
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
}
