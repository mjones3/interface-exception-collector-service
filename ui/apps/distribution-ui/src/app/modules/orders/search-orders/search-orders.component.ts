import { formatDate } from '@angular/common';
import { HttpResponse } from '@angular/common/http';
import { Component, Inject, LOCALE_ID, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import {
  Column,
  CustomerDto,
  CustomerService,
  DateValidator,
  LocationDto,
  LocationService,
  LookUpDto,
  Option,
  OrderService,
  ProcessHeaderService,
  ProcessProductDto,
  ValidationType,
} from '@rsa/commons';
import {
  OPEN_OPTION_VALUE,
  OrderStatuses,
  OrderSummary,
  ORDER_BILLING_CUSTOMER,
  ORDER_DELIVERY_TYPE,
  ORDER_LOCATION_TYPE_ID,
  ORDER_SHIPMENT_TYPE,
  ORDER_SHIPPING_CUSTOMER,
  ORDER_STATUS,
  ShipmentType,
} from '@rsa/distribution/core/models/orders.model';
import { orderFieldsMock } from '@rsa/distribution/data/mock/orders.mock';
import { rowExpansionTrigger } from '@rsa/distribution/shared/animations/row-expansion-trigger';
import * as moment from 'moment';
import { ToastrService } from 'ngx-toastr';
import { LazyLoadEvent } from 'primeng/api/lazyloadevent';
import { MultiSelect } from 'primeng/multiselect';
import { Table } from 'primeng/table';
import { Observable, of } from 'rxjs';
import { catchError, debounceTime, distinctUntilChanged, finalize, map, switchMap } from 'rxjs/operators';

const ALL_LABEL = 'all.label';

@Component({
  selector: 'rsa-search-orders',
  templateUrl: './search-orders.component.html',
  styleUrls: ['./search-orders.component.scss'],
  animations: [rowExpansionTrigger],
})
export class SearchOrdersComponent implements OnInit {
  readonly orderStatuses = OrderStatuses;
  readonly validationType = ValidationType;

  processProperties: Map<string, string> = new Map<string, string>();
  searchString: string;
  placeholder: string;
  enableSearchBtn: boolean;
  orderSearchGroup: FormGroup;
  orderFields: Option[] = [...orderFieldsMock];
  shipToCustomers: CustomerDto[] = [];
  billToCustomers: CustomerDto[] = [];
  customerFilterBy: string;
  statuses: LookUpDto[] = [];
  deliveryTypes: LookUpDto[] = [];
  allOption: LookUpDto = {
    id: 0,
    descriptionKey: ALL_LABEL,
    active: true,
    type: '',
    optionValue: ALL_LABEL,
  };
  columns: Column[] = [
    {
      field: 'id',
      header: 'shipment-id.label',
      sortable: true,
      default: true,
    },
    {
      field: 'orderNumber',
      header: 'order-number.label',
      sortable: true,
      default: true,
    },
    {
      field: 'shippingCustomerExternalId',
      header: 'ship-to-customer-id.label',
      sortable: true,
      hidden: true,
    },
    {
      field: 'shipLocationName',
      header: 'ship-to-location.label',
      sortable: true,
      hidden: true,
    },
    {
      field: 'createDate',
      header: 'create-date.label',
      templateRef: 'dateTpl',
      sortable: true,
      hidden: true,
    },
    {
      field: 'desireShippingDate',
      header: 'ship-date.label',
      templateRef: 'dateTpl',
      sortable: true,
      hidden: true,
    },
    {
      field: 'priority',
      header: 'priority.label',
      sortable: true,
      sortFieldName: 'priority',
      default: true,
    },
    {
      field: 'status',
      header: 'status.label',
      sortable: true,
      sortFieldName: 'status',
      default: true,
    },
    {
      field: 'createDate',
      header: 'create-date.label',
      sortable: true,
      sortFieldName: 'createDate',
      default: true,
    },
    {
      field: '',
      header: 'action.label',
      templateRef: 'actionTpl',
      hideHeader: true,
      default: true,
    },
  ];
  isExpandedAll = false;
  expandedRows: { [key: number]: boolean } = {};

  orders: OrderSummary[] = [];
  totalRecords = 0;
  loading = true;
  defaultRowsPerPage = 10;
  defaultSortField = 'priority';
  defaultLazyLoadEvent: LazyLoadEvent;
  shipmentTypes: LookUpDto[] = [];
  filteredLocations$: Observable<LocationDto[]>;

  activityValues: number[] = [0, 100];

  customerColumns = [
    'shippingCustomerExternalId',
    'shippingCustomerName',
    'billingCustomerExternalId',
    'billingCustomerName',
  ];
  chooseColumnsSelect: Column[] = this.columns.filter(c => c.field !== 'shipLocationName');
  hideColumnSelection = false;

  @ViewChild('shipToSelId') shipToSelId: MultiSelect;
  @ViewChild('billToSelId') billToSelId: MultiSelect;
  @ViewChild('orderTable') orderTable: Table;

  private _selectedColumns: Column[] = this.columns.filter(col => !col.hidden);

  constructor(
    private customerService: CustomerService,
    private orderService: OrderService,
    private toaster: ToastrService,
    private route: ActivatedRoute,
    private router: Router,
    private translateService: TranslateService,
    public header: ProcessHeaderService,
    public locationService: LocationService,
    protected fb: FormBuilder,
    @Inject(LOCALE_ID) public locale: string
  ) {
    this.createSearchFormGroup();
    this.defaultLazyLoadEvent = {
      first: 0,
      rows: this.defaultRowsPerPage,
      sortField: this.defaultSortField,
      sortOrder: 1, //ASC
    };
  }

  ngOnInit(): void {
    const orderConfigData = this.route.snapshot.data?.seachOrderConfigData as HttpResponse<
      LookUpDto[] | ProcessProductDto
    >[];

    if (orderConfigData?.length) {
      const lookUpRes = orderConfigData[0];
      const processRes = orderConfigData[1];

      this.statuses = (lookUpRes?.body as LookUpDto[])?.filter(lookUp => lookUp.type === ORDER_STATUS);
      this.deliveryTypes = (lookUpRes?.body as LookUpDto[])?.filter(lookUp => lookUp.type === ORDER_DELIVERY_TYPE);
      this.processProperties = (processRes?.body as ProcessProductDto)?.properties;
      this.shipmentTypes = (lookUpRes?.body as LookUpDto[])?.filter(lookUp => lookUp.type === ORDER_SHIPMENT_TYPE);

      this.shipmentTypesChange(this.shipmentTypes.find(type => this.isCustomerShipmentType(type)));
      this.initialFormValues();
    }
  }

  private initialFormValues() {
    this.orderSearchGroup.patchValue({
      statusKey: [this.openStatus],
      shipmentType: this.shipmentTypes.find(type => this.isCustomerShipmentType(type)),
    });
  }

  fetchOrders(event?: LazyLoadEvent) {
    //Cleaning the data when another search or a pagination is done
    this.loading = true;
    this.orders = [];
    this.totalRecords = 0;

    if (!event) {
      event = this.defaultLazyLoadEvent;
    }

    this.orderService
      .getOrdersSummaryByCriteria({}, true)
      .pipe(finalize(() => (this.loading = false)))
      .subscribe(
        response => {
          if (response.data.listShipments) {
            this.orderTable.sortField = event.sortField ?? this.defaultSortField;
            this.orders =
              response.data?.listShipments?.map(order => {
                const orderSummary: OrderSummary = {
                  ...order,
                  deliveryTypeDescriptionKey:
                    this.deliveryTypes?.find(dt => dt.optionValue === order.deliveryType)?.descriptionKey ?? '',
                  statusDescriptionKey:
                    this.statuses?.find(s => s.optionValue === order.statusKey)?.descriptionKey ?? '',
                  statusColor: this.processProperties[this.orderStatuses[order.statusKey]],
                };
                return orderSummary;
              }) ?? [];
            this.totalRecords = Number(0);
          } else {
            this.toaster.error('no-results-found.label');
          }
        },
        err => {
          this.toaster.error('something-went-wrong.label');
          throw err;
        }
      );
  }

  // TO BE FIXED WHEN WORKING ON SEARCH ORDER
  details(shipment: OrderSummary) {
    this.router.navigateByUrl(`/shipment/${shipment.id}/shipment-details`);
  }

  expandAll() {
    if (!this.isExpandedAll) {
      this.orders.forEach(data => {
        if (data.comments) {
          this.expandedRows[data.id] = true;
        }
      });
    } else {
      this.expandedRows = {};
    }
    this.isExpandedAll = !this.isExpandedAll;
  }

  onRowExpand() {
    if (Object.keys(this.expandedRows).length === this.orders.filter(o => o.comments).length) {
      this.isExpandedAll = true;
    }
  }

  onRowCollapse() {
    if (Object.keys(this.expandedRows).length === 0) {
      this.isExpandedAll = false;
    }
  }

  get selectedColumns(): Column[] {
    return this._selectedColumns;
  }

  set selectedColumns(val: Column[]) {
    //restore original order
    this._selectedColumns = this.columns.filter(col => val.includes(col));
  }

  get openStatus() {
    return this.statuses.find(status => status.optionValue === OPEN_OPTION_VALUE) ?? null;
  }

  private createSearchFormGroup() {
    this.orderSearchGroup = this.fb.group({
      shipmentType: ['', Validators.required],
      order: [],
      externalId: '',
      orderNumber: '',
      statusKey: [],
      deliveryType: [],
      createDateFrom: '',
      createDateTo: '',
      desireShippingDateFrom: '',
      desireShippingDateTo: '',
    });
  }

  //#region FILTERS
  get isInternalTransfersSelected(): boolean {
    return this.isInternalTransfersType(this.orderSearchGroup.get('shipmentType').value);
  }

  setSearchPlaceholder(event) {
    this.searchStringChange(this.searchString);
    this.placeholder = event?.value?.length
      ? `[${this.translateService.instant('order.label')} = ${event.value
          .map(v => this.translateService.instant(v.descriptionKey))
          .join(', ')}]`
      : '';
  }

  resetFilters() {
    this.searchString = '';
    this.placeholder = '';
    this.orderSearchGroup.reset();
    this.initialFormValues();
  }

  applyFilters(): void {
    this.setUserCriteria();
    if (this.isInternalTransfersSelected) {
      this._selectedColumns = this.columns.filter(column => !this.customerColumns.includes(column.field));
      this.hideColumnSelection = true;
    } else {
      this._selectedColumns = this.columns.filter(col => !col.hidden);
      this.hideColumnSelection = false;
    }
    this.orderTable.reset();
  }

  customerFilter(event, fieldName: 'shippingCustomerExternalId' | 'billingCustomerExternalId') {
    const isNotANumber = event.filter ? isNaN(event.filter) : true;

    if ((isNotANumber && event?.filter?.length >= 3) || !isNotANumber) {
      const addressType =
        fieldName === 'shippingCustomerExternalId'
          ? { addressType: ORDER_SHIPPING_CUSTOMER }
          : { addressType: ORDER_BILLING_CUSTOMER };
      const filter = isNotANumber
        ? { ...addressType, 'name.contains': event.filter }
        : { ...addressType, 'externalId.in': event.filter };
      this.customerFilterBy = isNotANumber ? 'name' : 'externalId';
      this.customerService.getCustomerByCriteria({ ...filter, active: 'true' }).subscribe(customerRes => {
        if (fieldName === 'shippingCustomerExternalId') {
          this.shipToCustomers = customerRes?.body ?? [];
          if (this.shipToSelId) {
            this.shipToSelId.options = this.shipToCustomers;
            this.shipToSelId.activateFilter();
          }
        } else {
          this.billToCustomers = customerRes?.body ?? [];
          if (this.billToSelId) {
            this.billToSelId.options = this.billToCustomers;
            this.billToSelId.activateFilter();
          }
        }
      });
    } else if (!event.filter?.length) {
      if (fieldName === 'shippingCustomerExternalId') {
        this.shipToSelId.options = this.selectedShipToCustomers ?? [];
        this.shipToSelId.activateFilter();
      } else {
        this.billToSelId.options = this.selectedBillToCustomers ?? [];
        this.billToSelId.activateFilter();
      }
    }
  }

  private isCustomerShipmentType(shipmentType: LookUpDto) {
    return shipmentType?.optionValue === ShipmentType.CUSTOMER;
  }

  private isInternalTransfersType(shipmentType: LookUpDto) {
    return shipmentType?.optionValue === ShipmentType.INTERNAL;
  }

  removeCustomer(chip: CustomerDto, fieldName: 'shippingCustomerExternalId' | 'billingCustomerExternalId') {
    const selectedCustomers = this.orderSearchGroup?.get(fieldName)?.value;

    if (selectedCustomers?.length) {
      const index = selectedCustomers.indexOf(chip);
      if (index > -1) {
        selectedCustomers.splice(index, 1);
        this.orderSearchGroup?.get(fieldName).setValue(selectedCustomers);
      }
    }
  }

  searchStringChange(event) {
    this.enableSearchBtn = false;

    if (
      event?.trim().length > 0 &&
      this.orderSearchGroup?.value?.order?.length ===
        event
          .trim()
          .split(',')
          ?.filter(element => element).length
    ) {
      this.enableSearchBtn = true;
    }
  }

  dateChange(
    event,
    fromFieldName: 'createDateFrom' | 'desireShippingDateFrom',
    toFieldName: 'createDateTo' | 'desireShippingDateTo'
  ) {
    const value = event?.currentTarget?.value;
    const fieldName = event?.currentTarget?.id;
    const changedField = this.orderSearchGroup?.get(fieldName);

    if (value?.length === 10) {
      if (Date.parse(value)) {
        const fromField = this.orderSearchGroup?.get(fromFieldName);
        const toField = this.orderSearchGroup?.get(toFieldName);

        const maxDateValue = fieldName === fromFieldName ? toField?.value : null;
        const minDateValue = fieldName === toFieldName ? fromField?.value : null;

        changedField?.setValidators(DateValidator.isOutOfRange(maxDateValue, minDateValue));
        changedField?.setValue(new Date(value));

        if (changedField.valid) {
          if (fieldName === fromFieldName && !toField?.valid && toField?.value) {
            toField.setErrors(null);
          } else if (fieldName === toFieldName && !fromField?.valid && fromField?.value) {
            fromField.setErrors(null);
          }
        }
      } else {
        changedField?.clearValidators();
        changedField?.setErrors({ outOfDate: true });
      }
    }
  }

  checkAllOptions(selected, formControlName) {
    if (selected) {
      let value;
      const allValue = this.allOption;
      if (formControlName === 'statusKey') {
        value = this.statuses;
      } else {
        value = this.deliveryTypes;
      }
      this.orderSearchGroup.controls[formControlName].patchValue([allValue, ...value]);
    } else {
      this.orderSearchGroup.controls[formControlName].patchValue([]);
    }
  }

  checkOneOption(all, formControlName) {
    if (all.selected) {
      all.deselect();
      return;
    }

    if (
      (formControlName === 'statusKey' &&
        this.orderSearchGroup.get(formControlName).value.length === this.statuses.length) ||
      (formControlName === 'deliveryType' &&
        this.orderSearchGroup.get(formControlName).value.length === this.deliveryTypes.length)
    ) {
      all.select();
    }
  }

  get selectedShipToCustomers() {
    return this.orderSearchGroup?.get('shippingCustomerExternalId')?.value ?? [];
  }

  get selectedBillToCustomers() {
    return this.orderSearchGroup?.get('billingCustomerExternalId')?.value ?? [];
  }

  get filters() {
    const filter = {};
    const formValue = this.orderSearchGroup.value;

    if (formValue.orderNumber) {
      filter['orderNumber'] = formValue.orderNumber;
    }

    if (formValue.externalId) {
      filter['externalId'] = formValue.externalId;
    }

    if (formValue?.shippingCustomerExternalId?.length) {
      filter['shippingCustomerId.in'] = formValue?.shippingCustomerExternalId.map(customer => customer.id).join(',');
    }

    if (formValue?.billingCustomerExternalId?.length) {
      filter['billingCustomerId.in'] = formValue?.billingCustomerExternalId.map(customer => customer.id).join(',');
    }

    if (formValue?.shipToLocation?.id) {
      filter['shippingLocationId'] = formValue.shipToLocation.id;
    }

    if (formValue.statusKey?.length) {
      filter['statusKey.in'] = formValue.statusKey.map(status => status.optionValue).join(',');
    }

    if (formValue.shipmentType?.optionValue) {
      filter['shipmentType'] = formValue.shipmentType.optionValue;
    }

    if (formValue.deliveryType?.length) {
      filter['deliveryType.in'] = formValue.deliveryType.map(delivery => delivery.optionValue).join(',');
    }

    if (formValue.createDateFrom) {
      filter['createDate.greaterThanOrEqual'] = `${formatDate(
        formValue.createDateFrom,
        'YYYY-MM-ddT00:00:00.000',
        this.locale
      )}Z`;
    }

    if (formValue.createDateTo) {
      filter['createDate.lessThanOrEqual'] = `${formatDate(
        formValue.createDateTo,
        'YYYY-MM-ddT23:59:59.999',
        this.locale
      )}Z`;
    }

    if (formValue.desireShippingDateFrom) {
      filter['desireShippingDate.greaterThanOrEqual'] = moment(formValue.desireShippingDateFrom)
        .utcOffset(0, true)
        .format('YYYY-MM-DD');
    }

    if (formValue.desireShippingDateTo) {
      filter['desireShippingDate.lessThanOrEqual'] = moment(formValue.desireShippingDateTo)
        .utcOffset(0, true)
        .format('YYYY-MM-DD');
    }

    return filter;
  }

  private setUserCriteria() {
    if (this.searchString?.trim().length > 0 && this.orderSearchGroup.get('order')?.value?.length) {
      const options = this.orderSearchGroup.get('order')?.value;

      const userCriteria = this.searchString?.trim().split(',') ?? [];
      userCriteria.forEach((value, index) => {
        value = value.trim();
        if (options?.length && options[index]) {
          this.orderSearchGroup?.controls[options[index].selectionKey]?.setValue(value);
        }
      });
    }
  }

  displayLocationFn(location: LocationDto): string {
    return location ? `${location.name}` : '';
  }

  shipmentTypesChange($event: LookUpDto) {
    if (this.isInternalTransfersType($event)) {
      setTimeout(() => {
        this.orderSearchGroup.removeControl('shippingCustomerExternalId');
        this.orderSearchGroup.removeControl('billingCustomerExternalId');
      });

      this.orderSearchGroup.addControl('shipToLocation', new FormControl());
      this.filteredLocations$ = this.orderSearchGroup.get('shipToLocation').valueChanges.pipe(
        debounceTime(400),
        distinctUntilChanged(),
        map(val => (typeof val === 'string' ? val : null)), // Added because of `displayFn`, which makes the `valueChanges` to be called again.
        switchMap((searchString: string | null) => {
          if (searchString) {
            return this.locationService
              .getAllLocationsByCriteria({
                'name.contains': searchString,
                active: true,
                'locationTypeId.in': ORDER_LOCATION_TYPE_ID,
              })
              .pipe(
                catchError(() => of({ body: [] } as HttpResponse<LocationDto[]>)),
                map(locationRes => locationRes.body)
              );
          }
          return of([]);
        })
      );
    } else {
      this.orderSearchGroup.addControl('shippingCustomerExternalId', new FormControl(''));
      this.orderSearchGroup.addControl('billingCustomerExternalId', new FormControl(''));

      this.orderSearchGroup.removeControl('shipToLocation');
    }
  }
}
