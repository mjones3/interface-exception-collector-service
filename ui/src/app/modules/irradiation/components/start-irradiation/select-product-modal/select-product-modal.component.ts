import {
    Component,
    inject, OnInit
} from '@angular/core';
import {
    MAT_DIALOG_DATA, MatDialog, MatDialogModule,
    MatDialogRef
} from '@angular/material/dialog';

import {ActionButtonComponent} from "../../../../../shared/components/buttons/action-button.component";
import {OptionsPickerComponent} from "../../../../../shared/components/options-picker/options-picker.component";
import { ImportDetailsData, ImportDetailsModal } from '../blood-center-information-modal/blood-center-information.component';

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
    private matDialog = inject(MatDialog);
    selectOption(option: any) {
        if (option.isImported){
            this.openBloodCenterInformationModal(option);
        } else {
            this.dialogRef.close(option);
        } 
    }
    private openBloodCenterInformationModal(product: any) {
       const bloodCenterDialogRef = this.matDialog.open(ImportDetailsModal, {
            width: '500px',
            disableClose: true
        });

        bloodCenterDialogRef.afterClosed().subscribe((importDetails: ImportDetailsData) => {
            if (importDetails) {
                const productWithImportDetails = {
                    ...product,
                    importDetails
                };
                this.dialogRef.close(productWithImportDetails);
            }
        });
    }

}
