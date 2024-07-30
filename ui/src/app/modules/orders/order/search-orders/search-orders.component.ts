import { AsyncPipe, CommonModule } from '@angular/common';
import { Component, ViewChild } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { Router } from '@angular/router';
import { FuseAlertComponent, FuseAlertService } from '@fuse/components/alert';
import { FuseCardComponent } from '@fuse/components/card';
import { ShipmentInfoDto } from 'app/modules/shipments/models/shipment-info.dto';
import { ProcessHeaderComponent } from 'app/shared/components/process-header/process-header.component';
import { LookUpDto } from 'app/shared/models/look-up-dto';
import { Column } from 'app/shared/models/table.model';
import { ValidationType } from 'app/shared/pipes/validation.pipe';
import { ProcessHeaderService } from 'app/shared/services/process-header.service';
import { ToastrService } from 'ngx-toastr';
import { LazyLoadEvent } from 'primeng/api';
import { Table, TableModule } from 'primeng/table';
import { finalize } from 'rxjs';
import { OPEN_OPTION_VALUE, OrderStatuses, OrderSummary } from '../../models/order.model';
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
      FuseAlertComponent
    ],
  templateUrl: './search-orders.component.html',
})

export class SearchOrdersComponent {
  readonly validationType = ValidationType;
  readonly orderStatuses = OrderStatuses;
  processProperties: Map<string, string> = new Map<string, string>();
  statuses: LookUpDto[] = [];
  deliveryTypes: LookUpDto[] = [];
 
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
  orders: OrderSummary[] = [];
  shipmentInfo: ShipmentInfoDto;
  totalRecords = 0;
  loading = true;
  defaultRowsPerPage = 10;
  defaultSortField = 'priority';
  defaultLazyLoadEvent: LazyLoadEvent;
  shipmentTypes: LookUpDto[] = [];

  alertInfo = {
    type:null,
    message: null,
    title:null
  }

  @ViewChild('orderTable', { static: true }) orderTable: Table;
  private _selectedColumns: Column[] = this.columns.filter(col => !col.hidden);

  constructor(
    private orderService: OrderService,
    private router: Router,
    public header: ProcessHeaderService,
    private toaster: ToastrService,
    private fuseAlert: FuseAlertService
  ){}

  ngOnInit(): void  {}

  fetchOrders(event?: LazyLoadEvent) {
    //Cleaning the data when another search or a pagination is done
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
}