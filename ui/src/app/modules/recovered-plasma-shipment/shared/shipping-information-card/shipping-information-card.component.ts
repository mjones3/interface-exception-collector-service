import { DatePipe } from '@angular/common';
import {
    Component,
    computed,
    EventEmitter,
    inject,
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
    datePipe = inject(DatePipe);

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
            transportationReferenceNumber: string;
        }>
    >();

    protected shipping = computed<Description[]>(() => [
        {
            label: 'Shipment Number',
            value: this.shippingInput()?.shipmentNumber,
        },
        {
            label: 'Customer Code',
            value: this.shippingInput()?.customerCode,
        },
        {
            label: 'Customer Name',
            value: this.shippingInput()?.customerName,
        },
        {
            label: 'Shipment Status',
            value: this.shippingInput()?.status,
        },
        {
            label: 'Product Type',
            value: ProductFamilyMap[this.shippingInput()?.productType],
        },
        {
            label: 'Shipment Date',
            value: this.datePipe.transform(
                this.shippingInput()?.shipmentDate,
                'MM/dd/yyyy'
            ),
        },
        {
            label: 'Total Cartons',
            value: this.shippingInput()?.totalCartons,
        },
        {
            label: 'Total Products',
            value: this.shippingInput()?.totalProducts,
        },
        {
            label: 'Total Volume',
            value: this.shippingInput()?.totalVolume,
        },
        {
            label: 'Transportation #',
            value: this.shippingInput()?.transportationReferenceNumber,
        },
    ]);

    handleButtonClick(): void {
        this.handleClick.emit();
    }
}
