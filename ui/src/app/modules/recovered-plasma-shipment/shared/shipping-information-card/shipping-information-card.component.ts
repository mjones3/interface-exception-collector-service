import {
    Component,
    computed,
    EventEmitter,
    Input,
    input,
    Output,
} from '@angular/core';
import { MatDivider } from '@angular/material/divider';
import {
    Description,
    DescriptionCardComponent,
    WidgetComponent,
} from '@shared';
import { ProductFamilyMap } from 'app/shared/models/product-family.model';

@Component({
    selector: 'biopro-shipping-information-card',
    standalone: true,
    imports: [DescriptionCardComponent, WidgetComponent, MatDivider],
    templateUrl: './shipping-information-card.component.html',
    styleUrl: './shipping-information-card.component.scss',
})
export class ShippingInformationCardComponent {
    @Input() showBasicButton = false;
    @Input() isButtonDisabled = true;
    @Output() handleClick = new EventEmitter<void>();

    protected shippingInput = input<
        Partial<{
            shipmentNumber: string;
            customerCode: string;
            customerName: string;
            status: string;
            productType: string;
            shipmentDate: string;
            totalCartons: number;
            totalProducts: number;
            totalVolume: number;
            transporationReferenceNumber: string;
        }>
    >();

    protected shipping = computed<Description[]>(() => [
        ...(this.shippingInput()?.shipmentNumber
            ? [
                  {
                      label: 'Shipment Number',
                      value: this.shippingInput()?.shipmentNumber,
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
        ...(this.shippingInput()?.productType
            ? [
                  {
                      label: 'Product Type',
                      value: ProductFamilyMap[
                          this.shippingInput()?.productType
                      ],
                  },
              ]
            : []),
        ...(this.shippingInput()?.shipmentDate
            ? [
                  {
                      label: 'Shipment Date',
                      value: this.shippingInput()?.shipmentDate,
                  },
              ]
            : []),
        ...(this.shippingInput()
            ? [
                  {
                      label: 'Total Cartons',
                      value: this.shippingInput()?.totalCartons,
                  },
              ]
            : []),
        ...(this.shippingInput()
            ? [
                  {
                      label: 'Total Products',
                      value: '1212121',
                  },
              ]
            : []),
        ...(this.shippingInput()
            ? [
                  {
                      label: 'Total Volume',
                      value: this.shippingInput()?.totalVolume,
                  },
              ]
            : []),
        ...(this.shippingInput()
            ? [
                  {
                      label: 'Transportation #',
                      value: this.shippingInput()?.transporationReferenceNumber,
                  },
              ]
            : []),
    ]);

    handleButtonClick() {
        this.handleClick.emit();
    }
}
