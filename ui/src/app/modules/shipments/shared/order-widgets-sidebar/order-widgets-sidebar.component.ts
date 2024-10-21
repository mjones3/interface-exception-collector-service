import { CommonModule } from '@angular/common';
import { Component, computed, input } from '@angular/core';
import { MatExpansionModule } from '@angular/material/expansion';
import {
    Description,
    DescriptionCardComponent,
    WidgetComponent,
} from '@shared';
import { OrderStatusMap } from '../../../../shared/models/order-status.model';

@Component({
    standalone: true,
    selector: 'app-order-widget-sidebar',
    templateUrl: './order-widgets-sidebar.component.html',
    styleUrls: ['./order-widgets-sidebar.component.scss'],
    imports: [
        CommonModule,
        WidgetComponent,
        DescriptionCardComponent,
        MatExpansionModule,
    ],
})
export class OrderWidgetsSidebarComponent {
    protected productInput = input<
        Partial<{
            id: number;
            productFamily: string;
            bloodType: string;
            temperatureCategory: string;
        }>
    >();

    protected orderInput = input<
        Partial<{
            id: number;
            externalId: string;
            priority: string;
            status: string;
            labelingProductCategory: string;
        }>
    >();

    protected comments = input<string>();

    protected shippingInput = input<
        Partial<{
            id: number;
            customerCode: string;
            customerName: string;
            status: string;
            method: string;
        }>
    >();

    protected billingInput = input<
        Partial<{
            customerCode: string;
            customerName: string;
        }>
    >();

    protected order = computed<Description[]>(() => [
        ...(this.orderInput()?.id
            ? [
                  {
                      label: 'BioPro Order ID',
                      value: this.orderInput()?.id?.toString(),
                  },
              ]
            : []),
        ...(this.orderInput()?.externalId
            ? [
                  {
                      label: 'External Order ID',
                      value: this.orderInput()?.externalId,
                  },
              ]
            : []),
        ...(this.orderInput()?.priority
            ? [
                  {
                      label: 'Priority',
                      value: this.orderInput()?.priority,
                  },
              ]
            : []),
        ...(this.orderInput()?.status
            ? [
                  {
                      label: 'Status',
                      value:
                          OrderStatusMap?.[this.orderInput()?.status] ??
                          'Unknown',
                  },
              ]
            : []),
        ...(this.orderInput()?.labelingProductCategory
            ? [
                  {
                      label: 'Labeling Product Category',
                      value: this.orderInput()?.labelingProductCategory,
                  },
              ]
            : []),
    ]);

    protected shipping = computed<Description[]>(() => [
        ...(this.shippingInput()?.id
            ? [
                  {
                      label: 'Shipment ID',
                      value: this.shippingInput()?.id.toString(),
                  },
              ]
            : []),
        ...(this.shippingInput()?.customerCode
            ? [
                  {
                      label: 'Customer Code',
                      value: this.shippingInput()?.customerCode,
                  },
              ]
            : []),
        ...(this.shippingInput()?.customerName
            ? [
                  {
                      label: 'Customer Name',
                      value: this.shippingInput()?.customerName,
                  },
              ]
            : []),
        ...(this.shippingInput()?.status
            ? [
                  {
                      label: 'Status',
                      value: this.shippingInput()?.status,
                  },
              ]
            : []),
        ...(this.shippingInput()?.method
            ? [
                  {
                      label: 'Shipping Method',
                      value: this.shippingInput()?.method,
                  },
              ]
            : []),
    ]);

    protected billing = computed<Description[]>(() => [
        ...(this.billingInput()?.customerCode
            ? [
                  {
                      label: 'Billing Customer Code',
                      value: this.billingInput()?.customerCode,
                  },
              ]
            : []),
        ...(this.billingInput()?.customerName
            ? [
                  {
                      label: 'Billing Customer Name',
                      value: this.billingInput()?.customerName,
                  },
              ]
            : []),
    ]);

    protected productComments = input<string>();

    protected productDetails = computed<Description[]>(() => [
        ...(this.productInput()?.temperatureCategory
            ? [
                  {
                      label: 'Temperature Category',
                      value: this.productInput()?.temperatureCategory,
                  },
              ]
            : []),
        ...(this.productInput()?.productFamily
            ? [
                  {
                      label: 'Product Family',
                      value: this.productInput()?.productFamily,
                  },
              ]
            : []),
        ...(this.productInput()?.bloodType
            ? [
                  {
                      label: 'Blood Type',
                      value: this.productInput()?.bloodType,
                  },
              ]
            : []),
    ]);
}
