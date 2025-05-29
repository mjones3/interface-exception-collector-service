import { CommonModule } from '@angular/common';
import { Component, computed, Inject, LOCALE_ID, OnInit, signal, TemplateRef, viewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ProcessHeaderComponent, ProcessHeaderService, TableConfiguration } from '@shared';
import { TableComponent } from 'app/shared/components/table/table.component';
import { RecoveredPlasmaShipmentCommon } from '../../recovered-plasma-shipment.common';
import { Store } from '@ngrx/store';
import { ToastrService } from 'ngx-toastr';
import { RecoveredPlasmaService } from '../../services/recovered-plasma.service';
import { CookieService } from 'ngx-cookie-service';
import { ProductIconsService } from 'app/shared/services/product-icon.service';
import { RecoveredPlasmaShipmentDetailsNavbarComponent } from '../recovered-plasma-shipment-details-navbar/recovered-plasma-shipment-details-navbar.component';
import { RecoveredPlasmaShipmentService } from '../../services/recovered-plasma-shipment.service';
import { ShipmentHistoryDTO } from '../../graphql/query-definitions/shipment-comments-history.graphql';

@Component({
  selector: 'biopro-shipment-comments',
  standalone: true,
  imports: [
    TableComponent, 
    CommonModule, 
    ProcessHeaderComponent,
    RecoveredPlasmaShipmentDetailsNavbarComponent
  ],
  templateUrl: './shipment-comments.component.html'
})
export class ShipmentCommentsComponent extends RecoveredPlasmaShipmentCommon implements OnInit{
  shipmentHistoryData= signal<ShipmentHistoryDTO[]>(null)

  protected cartonRoutesComputed = computed(
    () => `/recovered-plasma/${this.route.snapshot.params?.id}/shipment-details`
  );

  createDateTemplateRef = viewChild<TemplateRef<Element>>('createDateTemplateRef');

  shipmentInfoCommentsTableConfigComputed = computed<TableConfiguration>(() => ({
      showPagination: false,
      columns: [
          {
              id: 'createEmployeeId',
              header: 'Staff',
              sort: false,
          },
          {
            id: 'createDate',
            header: 'Date and Time',
            sort: false,
            columnTempRef: this.createDateTemplateRef(),
          },
          {
            id: 'comments',
            header: 'Comments',
            sort: false,
          }
      ],
  }));

  constructor(
    protected route: ActivatedRoute,
    public header: ProcessHeaderService,
    protected store: Store,
    protected router: Router,
    protected toastr: ToastrService,
    protected recoveredPlasmaService: RecoveredPlasmaService,
    protected cookieService: CookieService,
    protected productIconService: ProductIconsService,
    private shipmentService: RecoveredPlasmaShipmentService
  ){
    super(
      route,
      router,
      store,
      recoveredPlasmaService,
      toastr,
      productIconService,
      cookieService
  );
  }
  ngOnInit(): void {
    this.fetchShipmentCommentsHistory()
  }

  get shipmentId(): number {
    return parseInt(this.route.snapshot.params?.id);
  }

  fetchShipmentCommentsHistory(){
    this.shipmentService.getShipmentHistory(this.shipmentId).subscribe({
      next: (response) => {
        if (Array.isArray(response?.data?.findAllShipmentHistoryByShipmentId)) {
            const data = response.data.findAllShipmentHistoryByShipmentId;
            this.shipmentHistoryData.set(data);
        } else {
            this.shipmentHistoryData.set([]);
        }
    }
    });
  }
}
