import { AsyncPipe, CommonModule, formatDate } from '@angular/common';
import {
    Component,
    Inject,
    LOCALE_ID,
    OnInit,
    TemplateRef,
    ViewChild,
    computed,
    viewChild,
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatSortable, Sort } from '@angular/material/sort';
import { Router } from '@angular/router';
import { ApolloError, ApolloQueryResult } from '@apollo/client';
import { FuseCardComponent } from '@fuse/components/card/public-api';
import {
    Column,
    ProcessHeaderComponent,
    ProcessHeaderService,
    TableColumn,
    TableConfiguration,
} from '@shared';
import { TableComponent } from 'app/shared/components/table/table.component';
import { OrderStatusMap } from 'app/shared/models/order-status.model';
import { PriorityMap } from 'app/shared/models/product-family.model';
import { CookieService } from 'ngx-cookie-service';
import { ToastrService } from 'ngx-toastr';
import { Table, TableModule } from 'primeng/table';
import { finalize } from 'rxjs';
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
        TableComponent,
    ],
    templateUrl: './search-orders.component.html',
    styleUrls: ['./search-orders.component.scss'],
})
export class SearchOrdersComponent implements OnInit {
    protected readonly OrderStatusMap = OrderStatusMap;
    readonly PriorityMap = PriorityMap;

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
    readonly filterColumns: Column[] = [
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
    currentFilter: SearchOrderFilterDTO;
    loading = true;

    @ViewChild('orderTable', { static: false }) orderTable: Table;

    private _selectedColumns: Column[] = this.filterColumns.filter(
        (col) => !col.hidden
    );

    constructor(
        public header: ProcessHeaderService,
        private orderService: OrderService,
        private router: Router,
        private toaster: ToastrService,
        private cookieService: CookieService,
        @Inject(LOCALE_ID) public locale: string
    ) {}

    ngOnInit() {
        this.searchOrders();
    }

    // TODO
    protected readonly defaultSort = {
        id: 'bloodCenterID',
        start: 'asc',
    } as MatSortable;

    dataSource: OrderReportDTO[] = [];
    priorityTemplateRef = viewChild<TemplateRef<Element>>(
        'priorityTemplateRef'
    );
    customerNameTemplateRef = viewChild<TemplateRef<Element>>(
        'customerNameTemplateRef'
    );
    detialBtnTemplateRef = viewChild<TemplateRef<Element>>(
        'detialBtnTemplateRef'
    );
    createDateTemplateRef = viewChild<TemplateRef<Element>>(
        'createDateTemplateRef'
    );
    desireShipDateTemplateRef = viewChild<TemplateRef<Element>>(
        'desireShipDateTemplateRef'
    );
    columns = computed<TableColumn[]>(() => [
        {
            id: 'orderNumber',
            header: 'BioPro Order ID',
            sort: false,
            icon: false,
        },
        {
            id: 'externalId',
            header: 'External Order ID',
            sort: false,
            icon: false,
        },
        {
            id: 'orderPriorityReport.priority',
            header: 'Priority',
            sort: false,
            icon: false,
            columnTempRef: this.priorityTemplateRef(),
        },
        {
            id: 'orderStatus',
            header: 'Status',
            sort: false,
            icon: false,
        },
        {
            id: 'orderCustomerReport.name',
            header: 'Ship to Customer Name',
            sort: false,
            icon: false,
            columnTempRef: this.customerNameTemplateRef(),
        },
        {
            id: 'createDate',
            header: 'Create Date and Time',
            sort: false,
            icon: false,
            columnTempRef: this.createDateTemplateRef(),
        },
        {
            id: 'desireShipDate',
            header: 'Desired Ship Date',
            sort: false,
            icon: false,
            columnTempRef: this.desireShipDateTemplateRef(),
        },
        {
            id: 'action',
            header: '',
            columnTempRef: this.detialBtnTemplateRef(),
        },
    ]);
    tableConfig = computed<TableConfiguration>(() => {
        return {
            title: 'Results',
            columns: this.columns(),
            pageSize: 10,
            showPagination: false,
        };
    });

    sort(sort: Sort) {
        if (sort.active === 'deviceStatus' || sort.active === 'status') {
            this.dataSource.sort((a, b) => {
                const direction =
                    sort.direction === 'asc' || !sort.direction ? 1 : -1;
                return a[sort.active].label > b[sort.active].label
                    ? direction
                    : -direction;
            });
        }
    }

    searchOrders() {
        this.loading = true;
        this.dataSource = [];
        this.orderService
            .searchOrders(this.getCriteria())
            .pipe(finalize(() => (this.loading = false)))
            .subscribe({
                next: (response) => this.doOnSuccess(response),
                error: (e: ApolloError) => {
                    if (e?.cause?.message) {
                        this.dataSource = [];
                        this.toaster.warning(e?.cause?.message);
                        return;
                    }
                    this.toaster.error('Something went wrong.');
                    this.loading = false;
                    throw e;
                },
            });
    }

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
    private doOnSuccess(
        response: ApolloQueryResult<{ searchOrders: OrderReportDTO[] }>
    ): void {
        if (response.data.searchOrders.length === 1 && this.isFilterApplied()) {
            this.details(response.data.searchOrders[0].orderId);
        } else {
            this.dataSource = response?.data?.searchOrders;
        }
    }

    applyFilterSearch(searchCriteria: SearchOrderFilterDTO = {}): void {
        this.isFilterToggled = false;
        this.currentFilter = searchCriteria;
        this.searchOrders();
    }

    isFilterApplied() {
        return this.getCriteria()?.orderUniqueIdentifier != null;
    }

    details(id: number) {
        this.router.navigateByUrl(`/orders/${id}/order-details`);
    }

    get selectedColumns(): Column[] {
        return this._selectedColumns;
    }

    set selectedColumns(val: Column[]) {
        this._selectedColumns = this.filterColumns.filter((col) =>
            val.includes(col)
        );
    }

    protected readonly OrderPriorityMap = OrderPriorityMap;
}
