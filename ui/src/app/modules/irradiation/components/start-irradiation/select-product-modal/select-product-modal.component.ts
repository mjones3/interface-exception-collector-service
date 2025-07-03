import {
    Component,
    inject, OnInit
} from '@angular/core';
import {
    MAT_DIALOG_DATA, MatDialogModule,
    MatDialogRef
} from '@angular/material/dialog';

import {ActionButtonComponent} from "../../../../../shared/components/buttons/action-button.component";
import {OptionsPickerComponent} from "../../../../../shared/components/options-picker/options-picker.component";

@Component({
    selector: 'biopro-irradiation-select-product',
    templateUrl: './select-product-modal.component.html',
    standalone: true,
    imports: [
        ActionButtonComponent,
        OptionsPickerComponent,
        MatDialogModule,
        OptionsPickerComponent
    ]
})
export class IrradiationSelectProductModal {
    private dialogRef = inject(MatDialogRef<IrradiationSelectProductModal>);
    protected readonly data = inject(MAT_DIALOG_DATA) as {
        dialogTitle: string,
        options: [],
        optionsLabel: string
    };

    selectOption(option: any) {
        this.dialogRef.close(option);
    }
}
