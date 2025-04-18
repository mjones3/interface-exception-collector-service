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

    protected cartonInput = input<Partial<CartonDTO>>();

    protected carton = computed<Description[]>(() => [
        {
            label: 'Carton Number',
            value: this.cartonInput()?.cartonNumber,
        },
        {
            label: 'Total Volume (L)',
            value: this.cartonInput()?.totalVolume,
        },
        {
            label: 'Minimum Products',
            value: this.cartonInput()?.minNumberOfProducts,
        },
        {
            label: 'Maximum Products',
            value: this.cartonInput()?.maxNumberOfProducts,
        },
    ]);

    handleButtonClick(): void {
        this.handleClick.emit();
    }
}
