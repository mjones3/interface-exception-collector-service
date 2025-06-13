import { Component, inject } from '@angular/core';
import { ActionButtonComponent } from '../../../../shared/components/buttons/action-button.component';
import { MAT_DIALOG_DATA, MatDialogClose } from '@angular/material/dialog';
import { CartonDTO } from '../../models/recovered-plasma.dto';
import { MatButton } from '@angular/material/button';

@Component({
  selector: 'app-carton-print-actions-dialog',
  standalone: true,
    imports: [
        ActionButtonComponent,
        MatDialogClose,
        MatButton,
    ],
  templateUrl: './carton-print-actions-dialog.component.html'
})
export class CartonPrintActionsDialogComponent {

    static readonly PRINT_ACTION_PRINT_CARTON_LABEL = 'print-carton-label';
    static readonly PRINT_ACTION_CARTON_PACKING_SLIP = 'print-carton-packing-slip';

    protected data = inject<CartonDTO>(MAT_DIALOG_DATA);

    get actionPrintCartonLabel(): string {
        return CartonPrintActionsDialogComponent.PRINT_ACTION_PRINT_CARTON_LABEL;
    }

    get actionPrintCartonPackingSlip(): string {
        return CartonPrintActionsDialogComponent.PRINT_ACTION_CARTON_PACKING_SLIP;
    }

}
