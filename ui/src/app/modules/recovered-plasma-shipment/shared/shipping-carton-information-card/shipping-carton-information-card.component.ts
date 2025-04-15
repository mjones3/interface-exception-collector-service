import {
    Component,
    computed,
    EventEmitter,
    input,
    Input,
    Output,
} from '@angular/core';
import {
    Description,
    DescriptionCardComponent,
    WidgetComponent,
} from '@shared';
import {CartonDTO, RecoveredPlasmaShipmentResponseDTO} from '../../models/recovered-plasma.dto';

@Component({
    selector: 'biopro-shipping-carton-information-card',
    standalone: true,
    imports: [DescriptionCardComponent, WidgetComponent],
    templateUrl: './shipping-carton-information-card.component.html',
})
export class ShippingCartonInformationCardComponent {
    @Input() showBasicButton = false;
    @Input() isButtonDisabled = true;
    @Output() handleClick = new EventEmitter<void>();

    protected shippingInput = input<Partial<RecoveredPlasmaShipmentResponseDTO>>();
    protected cartonInput = input<Partial<CartonDTO>>();

    protected carton = computed<Description[]>(() => [
        {
            label: 'Carton Number',
            value: this.cartonInput()?.cartonNumber,
        },
        {
            label: 'Tare Weight (g)',
            value: this.shippingInput()?.cartonTareWeight,
        },
        {
            label: 'Total Volume (L)',
            value: this.cartonInput()?.totalVolume,
        },
        {
            label: 'Minimum Products',
        },
        {
            label: 'Maximum Products',
        },
    ]);

    handleButtonClick(): void {
        this.handleClick.emit();
    }
}
