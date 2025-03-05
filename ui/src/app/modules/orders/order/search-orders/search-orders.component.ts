import { AsyncPipe, CommonModule, formatDate } from '@angular/common';
import {
    Component,
    Inject,
    LOCALE_ID,
    OnInit,
    TemplateRef,
    ViewChild,
    computed,
    signal,
    viewChild,
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { PageEvent } from '@angular/material/paginator';
import { Router } from '@angular/router';
import { ApolloError, ApolloQueryResult } from '@apollo/client';
import {
    Column,
    ProcessHeaderComponent,
    ProcessHeaderService,
    TableColumn,
    TableConfiguration,
} from '@shared';
import { ERROR_MESSAGE } from 'app/core/data/common-labels';
import { TableComponent } from 'app/shared/components/table/table.component';
import { OrderStatusMap } from 'app/shared/models/order-status.model';
import { CookieService } from 'ngx-cookie-service';
import { ToastrService } from 'ngx-toastr';
import { Table, TableModule } from 'primeng/table';
import { OrderPriorityMap } from '../../../../shared/models/order-priority.model';
import { Cookie } from '../../../../shared/types/cookie.enum';
import { SearchOrderFilterDTO } from '../../models/order.dto';
import { PageDTO } from '../../models/page.model';
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
    readonly OrderPriorityMap = OrderPriorityMap;
    readonly OrderStatusMap = OrderStatusMap;

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

    page = signal<PageDTO<OrderReportDTO>>(null);

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

    statusTemplateRef = viewChild<TemplateRef<Element>>('statusTemplateRef');
    columns = computed<TableColumn[]>(() => [
        {
            id: 'orderNumber',
            header: 'BioPro Order ID',
            sort: false,
        },
        {
            id: 'externalId',
            header: 'External Order ID',
            sort: false,
        },
        {
            id: 'orderPriorityReport.priority',
            header: 'Priority',
            sort: false,
            columnTempRef: this.priorityTemplateRef(),
        },
        {
            id: 'orderStatus',
            header: 'Status',
            sort: false,
            columnTempRef: this.statusTemplateRef(),
        },
        {
            id: 'orderCustomerReport.name',
            header: 'Ship to Customer Name',
            sort: false,
            columnTempRef: this.customerNameTemplateRef(),
        },
        {
            id: 'createDate',
            header: 'Create Date and Time',
            sort: false,
            columnTempRef: this.createDateTemplateRef(),
        },
        {
            id: 'desireShipDate',
            header: 'Desired Ship Date',
            sort: false,
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
            pageSize: 20,
            showPagination: true,
        };
    });

    searchOrders() {
        this.loading = true;
        this.page.set(null);
        this.orderService.searchOrders(this.getCriteria()).subscribe({
            next: (response) => {
                this.loading = false;
                this.doOnSuccess(response);
            },
            error: (e: ApolloError) => {
                this.loading = false;
                if (e?.cause?.message) {
                    this.page.set(null);
                    this.toaster.warning(e?.cause?.message);
                    return;
                }
                this.toaster.error(ERROR_MESSAGE);
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
            if (this.currentFilter.page >= 0) {
                criteria.pageNumber = this.currentFilter.page;
            }
            criteria.pageSize =
                this.currentFilter.limit ?? this.tableConfig()?.pageSize;
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
        response: ApolloQueryResult<{ searchOrders: PageDTO<OrderReportDTO> }>
    ): void {
        if (
            response.data.searchOrders.content.length === 1 &&
            this.isFilterApplied()
        ) {
            this.details(response.data.searchOrders.content[0].orderId);
        } else {
            this.page.set(response?.data?.searchOrders);
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

    handlePagination(event: PageEvent): void {
        this.currentFilter = {
            ...this.currentFilter,
            page: event.pageIndex,
        };
        this.applyFilterSearch(this.currentFilter);
    }
}
