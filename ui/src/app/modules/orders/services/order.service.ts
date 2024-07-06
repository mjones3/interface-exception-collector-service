import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ApolloQueryResult } from '@apollo/client';
import { Apollo } from 'apollo-angular';
import { LIST_SHIPMENT } from 'app/modules/shipments/graphql/shipment/query-definitions/shipment.graphql';
import { CustomerDto } from 'app/shared/models/customer.dto';
import { Description } from 'app/shared/models/description.model';
import { LocationDto } from 'app/shared/models/location.dto';
import { LookUpDto } from 'app/shared/models/look-up-dto';
import { EnvironmentConfigService } from 'app/shared/services';
import moment from 'moment';
import { Observable, throwError } from 'rxjs';
import { OrderDto, OrderSummaryDto } from '../models/order.dto';


@Injectable({
  providedIn: 'root',
})
export class OrderService {
  orderEndpoint: string;
  orderSummaryEndpoint: string;
  nextOrderNumberEndpoint: string;
  orderBloodTypeEndpoint: string;
  orderProductAttributesEndpoint: string;
  orderProductFamiliesEndpoint: string;
  orderItemEndpoint: string;
  orderItemInventoryEndpoint: string;
  orderItemInventoryPendingToShipEndpoint: string;
  apolloClient: Apollo;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService, private apollo: Apollo) {
    this.orderEndpoint = config.env.serverApiURL + '/v1/orders';
    this.orderSummaryEndpoint = config.env.serverApiURL + '/v1/shipments';
    this.orderItemEndpoint = config.env.serverApiURL + '/v1/order-items';
    this.apolloClient = apollo;
  }

  //#region Descriptions

  private getLookUpDescriptionKey(optionValue: string, list: LookUpDto[]): string {
    return list.find(l => l.optionValue === optionValue)?.descriptionKey ?? '';
  }

  public getOrderInfoDescriptions(
    order: OrderDto,
    deliveryTypes: LookUpDto[],
    statuses: LookUpDto[],
    labelingProductCategory: string
  ): Description[] {
    return [
      { label: 'order-number.label', value: order?.orderNumber?.toString() },
      {
        label: 'priority.label',
        value: this.getLookUpDescriptionKey(order?.deliveryType, deliveryTypes),
      },
      {
        label: 'status.label',
        value: this.getLookUpDescriptionKey(order?.statusKey, statuses),
      },
      { label: 'shipment-id.label', value: order?.externalId || 'N/A' },
      { label: 'labeling-product-category.label', value: labelingProductCategory },
      ...(order.shipmentType === 'INTERNAL' ? [{ label: 'label-status.label', value: order.labelStatus }] : []),
    ];
  }

  public getShippingInfoDescriptions(
    shippingCustomer: CustomerDto,
    shippingMethod: string,
    desireShippingDate: string,
    shippingMethods: LookUpDto[],
    shipToLocation?: LocationDto
  ): Description[] {
    return [
      {
        label: shipToLocation ? 'ship-to-location-id.label' : 'ship-to-customer-id.label',
        value: shipToLocation ? `${shipToLocation.id}` : `${shippingCustomer?.externalId}`,
      },
      {
        label: shipToLocation ? 'ship-to-location.label' : 'ship-to-customer-name.label',
        value: shipToLocation ? `${shipToLocation.name}` : `${shippingCustomer?.name}`,
      },
      { label: 'ship-date.label', value: desireShippingDate ? moment(desireShippingDate).format('MM/DD/YYYY') : '' },
      {
        label: 'ship-method.label',
        value: this.getLookUpDescriptionKey(shippingMethod, shippingMethods),
      },
    ];
  }


  //#endregion

  public getOrdersSummaryByCriteria(
    criteria?: {},
    refetch: boolean = false
  ): Observable<ApolloQueryResult<{ listShipments: OrderSummaryDto[] }>> {  
    return this.apolloClient.query<{ listShipments: OrderSummaryDto[] }>({
      query: LIST_SHIPMENT,
      ...(refetch ? { fetchPolicy: 'network-only' } : {}),
    }); 
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
