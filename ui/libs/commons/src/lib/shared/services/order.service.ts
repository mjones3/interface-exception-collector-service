import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ApolloQueryResult } from '@apollo/client';
import { Apollo } from 'apollo-angular';
import * as moment from 'moment';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { GENERATE_PACKING_LIST_LABEL } from '../../schemas/graphql/packing-list/query-defintions/packing-list.graphql';
import { LIST_SHIPMENT } from '../../schemas/graphql/shipment/query-definitions/shipment.graphql';
import {
  CustomerDto,
  Description,
  LocationDto,
  LookUpDto,
  OrderBloodTypeDto,
  OrderDto,
  OrderItemAttachmentDto,
  OrderItemInventoryDto,
  OrderItemProductDTO,
  OrderProductAttributeDto,
  OrderProductFamilyDto,
  OrderServiceFeeDto,
  OrderSummaryDto,
  PackingListLabelDTO,
} from '../models';
import { EnvironmentConfigService } from './environment-config.service';

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
    this.nextOrderNumberEndpoint = config.env.serverApiURL + '/v1/orders/next-order-number';
    this.orderBloodTypeEndpoint = config.env.serverApiURL + '/v1/order-blood-types';
    this.orderProductAttributesEndpoint = config.env.serverApiURL + '/v1/order-product-attributes';
    this.orderProductFamiliesEndpoint = config.env.serverApiURL + '/v1/order-product-families';
    this.orderItemEndpoint = config.env.serverApiURL + '/v1/order-items';
    this.orderItemInventoryEndpoint = config.env.serverApiURL + '/v1/order-item-inventories';
    this.orderItemInventoryPendingToShipEndpoint =
      config.env.serverApiURL + '/v1/order-item-inventories/pending-to-be-shipped';
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

  public getBillInfoDescriptions(billingCustomer: CustomerDto): Description[] {
    return [
      { label: 'bill-to-customer-id.label', value: `${billingCustomer?.externalId}` },
      { label: 'bill-to-customer-name.label', value: `${billingCustomer?.name}` },
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

  public getServiceFeesInfoDescriptions(
    orderServiceFees: OrderServiceFeeDto[],
    serviceFees: LookUpDto[]
  ): Description[] {
    return orderServiceFees?.map(fee => ({
      label: this.getLookUpDescriptionKey(fee.serviceFee, serviceFees),
      value: fee.quantity?.toString(),
    }));
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

  public getOrderById(id: number): Observable<HttpResponse<OrderDto>> {
    return this.httpClient
      .get<OrderDto>(`${this.orderEndpoint}/${id}`, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getOrderByCriteria(criteria?: {}): Observable<HttpResponse<OrderDto[]>> {
    return this.httpClient
      .get<OrderDto[]>(this.orderEndpoint, { params: { ...criteria }, observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getOrderBloodTypeByCriteria(criteria?: {}): Observable<HttpResponse<OrderBloodTypeDto[]>> {
    return this.httpClient
      .get<OrderBloodTypeDto[]>(this.orderBloodTypeEndpoint, {
        params: { ...criteria, size: '1000' },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getOrderProductAttributesByCriteria(criteria?: {}): Observable<HttpResponse<OrderProductAttributeDto[]>> {
    return this.httpClient
      .get<OrderProductAttributeDto[]>(this.orderProductAttributesEndpoint, {
        params: { ...criteria, size: '1000' },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getOrderProductFamiliesByCriteria(criteria?: {}): Observable<HttpResponse<OrderProductFamilyDto[]>> {
    return this.httpClient
      .get<OrderProductFamilyDto[]>(this.orderProductFamiliesEndpoint, {
        params: { ...criteria, size: '1000' },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getNextOrderNumber(): Observable<HttpResponse<{ orderNumber: string }>> {
    return this.httpClient
      .get<{ orderNumber: string }>(this.nextOrderNumberEndpoint, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getOrderItemProductsByOrderItemIdAndCriteria(
    orderItemId: number,
    criteria?: {}
  ): Observable<HttpResponse<OrderItemProductDTO[]>> {
    return this.httpClient
      .get<OrderItemProductDTO[]>(`${this.orderItemEndpoint}/${orderItemId}/products`, {
        params: { ...criteria, size: '1000' },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getOrderItemInventoryByCriteria(criteria?: {}): Observable<HttpResponse<OrderItemInventoryDto[]>> {
    return this.httpClient
      .get<OrderItemInventoryDto[]>(this.orderItemInventoryEndpoint, { params: { ...criteria }, observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getOrderItemInventoriesPendingToShipByCriteria(
    criteria: {},
    isUnlabeled = false
  ): Observable<HttpResponse<OrderItemInventoryDto[]>> {
    return this.httpClient
      .get<OrderItemInventoryDto[]>(
        `${this.orderItemInventoryPendingToShipEndpoint}${isUnlabeled ? '-unlabeled' : ''}`,
        {
          params: { ...criteria },
          observe: 'response',
        }
      )
      .pipe(catchError(this.errorHandler));
  }

  public createOrder(order: OrderDto): Observable<OrderDto> {
    return this.httpClient.post(this.orderEndpoint, order).pipe(catchError(this.errorHandler));
  }

  public editOrder(id: number, order: OrderDto): Observable<OrderDto> {
    return this.httpClient.put(`${this.orderEndpoint}/${id}`, order).pipe(catchError(this.errorHandler));
  }

  public cancelOrder(id: number, cancelOrderReasonId: number): Observable<HttpResponse<OrderDto>> {
    return this.httpClient
      .patch<OrderDto>(
        `${this.orderEndpoint}/${id}/cancel`,
        { cancelOrderReasonId },
        {
          observe: 'response',
        }
      )
      .pipe(catchError(this.errorHandler));
  }

  public closeOrder(id: number, closeOrderReasonId?: number): Observable<HttpResponse<OrderDto>> {
    return this.httpClient
      .put<OrderDto>(
        `${this.orderEndpoint}/${id}/close`,
        { closeOrderReasonId },
        {
          observe: 'response',
        }
      )
      .pipe(catchError(this.errorHandler));
  }

  public removeOrderItemInventory(orderItemInventoryId: number): Observable<any> {
    return this.httpClient
      .delete(`${this.orderItemInventoryEndpoint}/${orderItemInventoryId}`, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public removeAllOrderItemInventory(orderItemId: number): Observable<any> {
    return this.httpClient
      .delete(`${this.orderItemEndpoint}/${orderItemId}/order-item-inventories`, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public saveFillOrder(
    orderItemId: number,
    obj: {
      orderItemInventories: OrderItemInventoryDto[];
      orderItemAttachments?: OrderItemAttachmentDto[];
    }
  ): Observable<
    HttpResponse<{
      orderItemInventories: OrderItemInventoryDto[];
      orderItemAttachments?: OrderItemAttachmentDto[];
    }>
  > {
    return this.httpClient
      .put(`${this.orderItemEndpoint}/${orderItemId}/fill`, obj)
      .pipe(catchError(this.errorHandler));
  }

  public deleteNotFilledInventories(orderItemId: number): Observable<any> {
    return this.httpClient
      .delete(`${this.orderItemEndpoint}/${orderItemId}/not-filled-inventories`, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
