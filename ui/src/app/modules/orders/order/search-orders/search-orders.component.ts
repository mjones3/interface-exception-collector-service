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
import { ToastrService } from 'ngx-toastr';
import { Table, TableLazyLoadEvent, TableModule } from 'primeng/table';
import { BehaviorSubject, Subject, finalize } from 'rxjs';
import { OrderSummary } from '../../models/order.model';
import { OrderService } from '../../services/order.service';
import { OrderReportDTO } from '../models/search-order.model';

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
    providers: [OrderService],
    templateUrl: './search-orders.component.html',
    styleUrls: ['./search-orders.component.scss'],
})
export class SearchOrdersComponent {
    readonly hiddenColumns: Column[] = [
        {
            field: 'shippingCustomerCode',
            header: 'Ship to Customer Code',
            sortable: true,
            hidden: true,
        },
        {
            field: 'billingCustomerName',
            header: 'Bill to Customer Name',
            sortable: true,
            hidden: true,
        },
        {
            field: 'billingCustomerCode',
            header: 'Bill to Customer Code',
            sortable: true,
            hidden: true,
        },
    ];
    readonly columns: Column[] = [
        {
            field: 'orderNumber',
            header: 'BioPro Order Number',
            sortable: true,
            default: true,
        },
        {
            field: 'externalId',
            header: 'External Order ID',
            sortable: true,
            default: true,
        },
        {
            field: 'priority',
            header: 'Priority',
            templateRef: 'priorityTpl',
            sortable: true,
            sortFieldName: 'priority',
            default: true,
        },
        {
            field: 'orderStatus',
            header: 'Status',
            sortable: true,
            sortFieldName: 'status',
            default: true,
        },
        {
            field: 'orderCustomerReport.name',
            header: 'Ship to Customer Name',
            templateRef: 'shipToCustomerTpl',
            sortable: true,
            default: true,
        },
        {
            field: 'createDate',
            header: 'Create Date and Time',
            sortable: true,
            templateRef: 'dateTimeTpl',
            sortFieldName: 'createDate',
            default: true,
        },
        {
            field: 'desireShipDate',
            header: 'Desired Ship Date',
            templateRef: 'dateTpl',
            sortable: true,
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
        public facilityService: FacilityService,
        public orderService: OrderService,
        public router: Router,
        public toaster: ToastrService,
        public header: ProcessHeaderService
    ) {}

    fetchOrders(event: TableLazyLoadEvent) {
        const facilityCode = this.facilityService.getFacilityCode();
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

    // TO BE FIXED WHEN WORKING ON SEARCH ORDER
    details(shipment: OrderSummary) {
        this.router.navigateByUrl(`/shipment/${shipment.id}/shipment-details`);
    }

    get selectedColumns(): Column[] {
        return this._selectedColumns;
    }

    set selectedColumns(val: Column[]) {
        this._selectedColumns = this.columns.filter((col) => val.includes(col));
    }
}
