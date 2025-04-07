import { AsyncPipe, DatePipe } from '@angular/common';
import {
    Component,
    OnInit,
    TemplateRef,
    computed,
    signal,
    viewChild,
} from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { PageEvent } from '@angular/material/paginator';
import { Sort } from '@angular/material/sort';
import { MatStepperModule } from '@angular/material/stepper';
import { Router } from '@angular/router';
import { ApolloError } from '@apollo/client';
import {
    LookUpDto,
    ProcessHeaderComponent,
    ProcessHeaderService,
    TableColumn,
    TableConfiguration,
} from '@shared';
import { ActionButtonComponent } from 'app/shared/components/buttons/action-button.component';
import { Cookie } from 'app/shared/types/cookie.enum';
import { CookieService } from 'ngx-cookie-service';
import { consumeUseCaseNotifications } from '../../../../shared/utils/notification.handling';
import { RecoveredPlasmaCustomerDTO } from '../../graphql/query-definitions/customer.graphql';
import { RecoveredPlasmaLocationDTO } from '../../graphql/query-definitions/location.graphql';
import { ToastrService } from 'ngx-toastr';
import { forkJoin, map, Observable, tap } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { TableComponent } from '../../../../shared/components/table/table.component';
import { OrderPriorityMap } from '../../../../shared/models/order-priority.model';
import { OrderStatusMap } from '../../../../shared/models/order-status.model';
import { EMPTY_PAGE, PageDTO } from '../../../../shared/models/page.model';
import handleApolloError from '../../../../shared/utils/apollo-error-handling';
import {
    RecoveredPlasmaShipmentQueryCommandRequestDTO,
    RecoveredPlasmaShipmentReportDTO,
    RecoveredPlasmaShipmentStatus,
} from '../../graphql/query-definitions/shipment.graphql';
import { RecoveredPlasmaService } from '../../services/recovered-plasma.service';
import { CreateShipmentComponent } from '../create-shipment/create-shipment.component';
import { FilterShipmentComponent } from '../filter-shipment/filter-shipment.component';
import { BasicButtonComponent } from '../../../../shared/components/buttons/basic-button.component';

@Component({
    selector: 'biopro-search-shipment',
    standalone: true,
    imports: [
        MatButtonModule,
        MatStepperModule,
        FormsModule,
        ReactiveFormsModule,
        ProcessHeaderComponent,
        MatFormFieldModule,
        MatInputModule,
        FilterShipmentComponent,
        ActionButtonComponent,
        AsyncPipe,
        TableComponent,
        DatePipe,
        BasicButtonComponent,
    ],
    templateUrl: './search-shipment.component.html',
})
export class SearchShipmentComponent implements OnInit {
    isFilterToggled = false;
    currentFilter: RecoveredPlasmaShipmentQueryCommandRequestDTO;

    shipmentDateTemplateRef = viewChild<TemplateRef<Element>>(
        'shipmentDateTemplateRef'
    );
    statusTemplateRef = viewChild<TemplateRef<Element>>('statusTemplateRef');
    detailBtnTemplateRef = viewChild<TemplateRef<Element>>(
        'detailBtnTemplateRef'
    );
    columns = computed<TableColumn[]>(() => [
        {
            id: 'shipmentNumber',
            header: 'Shipment Number',
            sort: false,
        },
        {
            id: 'customerName',
            header: 'Customer',
            sort: false,
        },
        {
            id: 'transportationReferenceNumber',
            header: 'Transportation #',
            sort: false,
        },
        {
            id: 'location',
            header: 'Location',
            sort: false,
        },
        {
            id: 'productType',
            header: 'Product Type',
            sort: false,
        },
        {
            id: 'shipmentDate',
            header: 'Shipment Date',
            sort: false,
            columnTempRef: this.shipmentDateTemplateRef(),
        },
        {
            id: 'status',
            header: 'Status',
            sort: false,
            columnTempRef: this.statusTemplateRef(),
        },
        {
            id: 'action',
            header: '',
            columnTempRef: this.detailBtnTemplateRef(),
        },
    ]);
    table = viewChild<TableComponent>('recoveredPlasmaShipmentTable');
    tableConfig = computed<TableConfiguration>(() => ({
        title: 'Results',
        columns: this.columns(),
        pageSize: 20,
        showPagination: false,
    }));

    loading = signal<boolean>(true);
    page = signal<PageDTO<RecoveredPlasmaShipmentReportDTO>>(null);
    locations = signal<RecoveredPlasmaLocationDTO[]>([]);
    customers = signal<RecoveredPlasmaCustomerDTO[]>([]);
    productTypes = signal<LookUpDto[]>([]);
    shipmentTypes = signal<LookUpDto[]>([]);

    isRecoveredPlasmaFacility = false;

    get facilityCode() {
        return this.cookieService.get(Cookie.XFacility);
    }

    constructor(
        public header: ProcessHeaderService,
        private toaster: ToastrService,
        private router: Router,
        private matDialog: MatDialog,
        private recoveredPlasmaService: RecoveredPlasmaService,
        private cookieService: CookieService
    ) {}

    ngOnInit(): void {
        this.load();
        this.checkRecoveredPlasmaFacility();
    }

    load(): void {
        forkJoin({
            searchShipments: this.loadRecoveredPlasmaShipments(),
            locationsResponse: this.loadLocations(),
            customersResponse: this.loadCustomers(),
            productTypes: this.loadProductTypes(),
            shipmentTypes: this.loadShipmentTypes(),
        }).subscribe(
            ({
                searchShipments,
                locationsResponse,
                customersResponse,
                productTypes,
                shipmentTypes,
            }) => {
                this.page.set(searchShipments);
                this.locations.set(locationsResponse);
                this.customers.set(customersResponse);
                this.productTypes.set(productTypes);
                this.shipmentTypes.set(shipmentTypes);
            }
        );
    }

    loadRecoveredPlasmaShipments(): Observable<
        PageDTO<RecoveredPlasmaShipmentReportDTO>
    > {
        this.loading.set(true);
        this.currentFilter = {
            ...(this.currentFilter?.shipmentNumber
                ? {}
                : { locationCode: [this.facilityCode] }),
            ...this.currentFilter,
        };
        return this.recoveredPlasmaService
            .searchRecoveredPlasmaShipments(this.currentFilter)
            .pipe(
                catchError((error: ApolloError) => {
                    this.loading.set(false);
                    this.page.set(EMPTY_PAGE);
                    handleApolloError(this.toaster, error);
                }),
                tap((response) => {
                    this.loading.set(false);
                    consumeUseCaseNotifications(
                        this.toaster,
                        response.data.searchShipment.notifications
                    );
                }),
                map(
                    (response) =>
                        response.data?.searchShipment?.data ?? EMPTY_PAGE
                )
            );
    }

    loadLocations(): Observable<RecoveredPlasmaLocationDTO[]> {
        return this.recoveredPlasmaService.findAllLocations().pipe(
            catchError((error: ApolloError) =>
                handleApolloError(this.toaster, error)
            ),
            map((response) => response.data?.findAllLocations ?? [])
        );
    }

    loadCustomers(): Observable<RecoveredPlasmaCustomerDTO[]> {
        return this.recoveredPlasmaService.findAllCustomers().pipe(
            catchError((error: ApolloError) =>
                handleApolloError(this.toaster, error)
            ),
            map((response) => response.data?.findAllCustomers ?? [])
        );
    }

    loadProductTypes(): Observable<LookUpDto[]> {
        return this.recoveredPlasmaService
            .findAllLookupsByType('RPS_PRODUCT_TYPE')
            .pipe(
                catchError((error: ApolloError) =>
                    handleApolloError(this.toaster, error)
                ),
                map((response) => response.data?.findAllLookupsByType ?? [])
            );
    }

    loadShipmentTypes(): Observable<LookUpDto[]> {
        return this.recoveredPlasmaService
            .findAllLookupsByType('RPS_SHIPMENT_TYPE')
            .pipe(
                catchError((error: ApolloError) =>
                    handleApolloError(this.toaster, error)
                ),
                map((response) => response.data?.findAllLookupsByType ?? [])
            );
    }

    checkRecoveredPlasmaFacility() {
        this.recoveredPlasmaService
            .checkRecoveredPlasmaFacility(this.facilityCode)
            .subscribe((res) => {
                this.isRecoveredPlasmaFacility = res;
            });
    }

    toggleFilter(toggleFlag: boolean): void {
        this.isFilterToggled = toggleFlag;
    }

    applyFilterSearch(
        searchCriteria: RecoveredPlasmaShipmentQueryCommandRequestDTO = {}
    ): void {
        this.isFilterToggled = false;
        this.currentFilter = searchCriteria;
        this.loadRecoveredPlasmaShipments().subscribe((searchShipments) =>
            this.page.set(searchShipments)
        );
    }

    resetFilterSearch() {
        this.currentFilter = {};
        this.table()
            .matSortRef()
            .sort({ id: '', start: '', disableClear: false });
    }

    openCreateShipment() {
        this.matDialog.open(CreateShipmentComponent, {
            width: '50rem',
            disableClose: true,
        });
    }

    handlePagination(event: PageEvent): void {
        this.currentFilter = {
            ...this.currentFilter,
            pageNumber: event.pageIndex,
        };
        this.applyFilterSearch(this.currentFilter);
    }

    handleSorting(sort: Sort): void {
        this.currentFilter = {
            ...this.currentFilter,
        };
        this.applyFilterSearch(this.currentFilter);
    }

    getStatusBadgeCssClass(status: keyof typeof RecoveredPlasmaShipmentStatus) {
        switch (status) {
            case 'OPEN':
                return 'text-sm font-bold py-1.5 px-2 badge rounded-full bg-gray-200 text-gray-700';
            case 'CLOSED':
                return 'text-sm font-bold py-1.5 px-2 badge rounded-full bg-blue-200 text-blue-700';
            default:
                return '';
        }
    }
    details(id: number) {
        this.router.navigateByUrl(`/recovered-plasma/${id}/shipment-details`);
    }

}
