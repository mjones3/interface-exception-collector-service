import { CommonModule } from '@angular/common';
import { Component, computed, input, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TableConfiguration } from '@shared';
import { ShipmentDetailResponseDTO } from 'app/modules/shipments/models/shipment-info.dto';
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
import { ApolloError } from '@apollo/client';
import { ERROR_MESSAGE } from 'app/core/data/common-labels';

@Component({
  selector: 'biopro-shipment-comments',
  standalone: true,
  imports: [
    TableComponent, 
    CommonModule, 
    RecoveredPlasmaShipmentDetailsNavbarComponent
  ],
  templateUrl: './shipment-comments.component.html'
})
export class ShipmentCommentsComponent extends RecoveredPlasmaShipmentCommon implements OnInit{
  shipmentDetails = input<ShipmentDetailResponseDTO>();

  shipmentHistoryData= signal<ShipmentHistoryDTO[]>([])

  protected cartonRoutesComputed = computed(
    () => `/recovered-plasma/${this.route.snapshot.params?.id}/shipment-details`
  );

  shipmentInfoCommentsTableConfigComputed = computed<TableConfiguration>(() => ({
      title: 'Shipment Comments',
      showPagination: false,
      columns: [
          {
              id: 'employeeId',
              header: 'Staff',
              sort: false,
          },
          {
            id: 'dateAndTime',
            header: 'Date and Time',
            sort: false,
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


  get shipmentId(): number{
    return this.shipmentDetails()?.id;
  }

  fetchShipmentCommentsHistory(){
    this.shipmentService.getShipmentHistory(this.shipmentId).subscribe({
      next: (response) => {
        if (Array.isArray(response?.data?.findAllShipmentHistoryByShipmentId)) {
            this.shipmentHistoryData.set(response?.data.findAllShipmentHistoryByShipmentId);
        } else {
            this.shipmentHistoryData.set([]);
        }
    },
    error: (error: ApolloError) => {
        this.toastr.error(ERROR_MESSAGE);
        throw error;
    },
  });
  }
}
