import { AsyncPipe, CommonModule, formatDate } from '@angular/common';
import {
    Component,
    Inject,
    LOCALE_ID,
    OnInit,
    TemplateRef,
    computed,
    signal,
    viewChild,
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { PageEvent } from '@angular/material/paginator';
import { Sort } from '@angular/material/sort';
import { Router } from '@angular/router';
import { ApolloError, ApolloQueryResult } from '@apollo/client';
import {
    ProcessHeaderComponent,
    ProcessHeaderService,
    TableColumn,
    TableConfiguration,
} from '@shared';
import { ERROR_MESSAGE } from 'app/core/data/common-labels';
import { TableComponent } from 'app/shared/components/table/table.component';
import { OrderStatusMap } from 'app/shared/models/order-status.model';
import { EMPTY_PAGE, PageDTO } from 'app/shared/models/page.model';
import { CookieService } from 'ngx-cookie-service';
import { ToastrService } from 'ngx-toastr';
import { OrderPriorityMap } from '../../../../shared/models/order-priority.model';
import { Cookie } from '../../../../shared/types/cookie.enum';
import { SearchOrderFilterDTO } from '../../models/order.dto';
import {
    OrderQueryCommandDTO,
    OrderReportDTO,
} from '../../models/search-order.model';
import { OrderService } from '../../services/order.service';
import { SearchOrderFilterComponent } from './search-filter/search-order-filter.component';
import { ShipmentTypeMap } from 'app/shared/models/shipment-type.model';

@Component({
    selector: 'app-search-orders',
    standalone: true,
    imports: [
        CommonModule,
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
    readonly ShipmentTypeMap = ShipmentTypeMap;

    isFilterToggled = false;
    currentFilter: SearchOrderFilterDTO;
    loading = signal<boolean>(true);

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
    shipmentTypeTemplateRef = viewChild<TemplateRef<Element>>(
        'shipmentTypeTemplateRef'
    );
    detailBtnTemplateRef = viewChild<TemplateRef<Element>>(
        'detailBtnTemplateRef'
    );
    createDateTemplateRef = viewChild<TemplateRef<Element>>(
        'createDateTemplateRef'
    );
    desireShipDateTemplateRef = viewChild<TemplateRef<Element>>(
        'desireShipDateTemplateRef'
    );
    statusTemplateRef = viewChild<TemplateRef<Element>>('statusTemplateRef');

    table = viewChild<TableComponent>('ordersTable');
    columns = computed<TableColumn[]>(() => [
        {
            id: 'orderNumber',
            header: 'BioPro Order ID',
            sort: true,
        },
        {
            id: 'externalId',
            header: 'External Order ID',
            sort: true,
        },
        {
            id: 'priority',
            header: 'Priority',
            sort: true,
            columnTempRef: this.priorityTemplateRef(),
        },
        {
            id: 'status',
            header: 'Status',
            sort: true,
            columnTempRef: this.statusTemplateRef(),
        },
        {
            id: 'shipmentType',
            header: 'Shipment Type',
            sort: true,
            columnTempRef: this.shipmentTypeTemplateRef(),
        },
        {
            id: 'shippingCustomerName',
            header: 'Ship To',
            sort: true,
            columnTempRef: this.customerNameTemplateRef(),
        },
        {
            id: 'createDate',
            header: 'Create Date and Time',
            sort: true,
            columnTempRef: this.createDateTemplateRef(),
        },
        {
            id: 'desiredShippingDate',
            header: 'Desired Ship Date',
            sort: true,
            columnTempRef: this.desireShipDateTemplateRef(),
        },
        {
            id: 'action',
            header: '',
            columnTempRef: this.detailBtnTemplateRef(),
        },
    ]);
    tableConfig = computed<TableConfiguration>(() => ({
        title: 'Results',
        columns: this.columns(),
        pageSize: 20,
        showPagination: true,
    }));

    searchOrders() {
        this.loading.set(true);
        this.page.set(null);
        this.orderService.searchOrders(this.getCriteria()).subscribe({
            next: (response) => {
                this.loading.set(false);
                this.doOnSuccess(response);
            },
            error: (e: ApolloError) => {
                this.loading.set(false);
                if (e?.cause?.message) {
                    this.page.set(EMPTY_PAGE);
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
        if (!this.currentFilter) {
            return criteria;
        }

        if (this.currentFilter.page >= 0) {
            criteria.pageNumber = this.currentFilter.page;
        }
        criteria.pageSize =
            this.currentFilter.limit ?? this.tableConfig()?.pageSize;
        if (this.currentFilter.orderNumber) {
            criteria.orderUniqueIdentifier = this.currentFilter.orderNumber;
        }
        if (this.currentFilter.orderStatus?.length) {
            criteria.orderStatus = this.currentFilter.orderStatus;
        }
        if (this.currentFilter.deliveryTypes?.length) {
            criteria.deliveryTypes = this.currentFilter.deliveryTypes;
        }
        if (this.currentFilter.customers?.length) {
            criteria.customers = this.currentFilter.customers;
        }
        //TODO
        if (this.currentFilter.shipmentType?.length) {
            criteria.shipmentType = this.currentFilter.shipmentType;
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
        if (this.currentFilter?.sortBy && this.currentFilter?.order) {
            const property = this.currentFilter.sortBy;
            const direction = this.currentFilter.order.toUpperCase() as
                | 'ASC'
                | 'DESC';
            criteria.querySort = { orderByList: [{ property, direction }] };
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

    resetFilterSearch() {
        this.currentFilter = {};
        this.table()
            .matSortRef()
            .sort({ id: '', start: '', disableClear: false });
    }

    isFilterApplied() {
        return this.getCriteria()?.orderUniqueIdentifier != null;
    }

    details(id: number) {
        this.router.navigateByUrl(`/orders/${id}/order-details`);
    }

    handlePagination(event: PageEvent): void {
        this.currentFilter = {
            ...this.currentFilter,
            page: event.pageIndex,
        };
        this.applyFilterSearch(this.currentFilter);
    }

    handleSorting(sort: Sort): void {
        this.currentFilter = {
            ...this.currentFilter,
            sortBy: sort.active,
            order: sort.direction,
        };
        this.applyFilterSearch(this.currentFilter);
    }
}
