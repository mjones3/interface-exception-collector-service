import { Component, inject, OnInit, signal, ViewEncapsulation } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import {
    MAT_DIALOG_DATA,
    MatDialogActions,
    MatDialogClose,
    MatDialogContent,
    MatDialogModule,
    MatDialogRef,
    MatDialogTitle
} from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { ActionButtonComponent } from '../../../../shared/components/buttons/action-button.component';
import { OptionsPickerComponent } from '../../../../shared/components/options-picker/options-picker.component';
import { OptionPicker } from '@shared';
import { ProductIconsService } from '../../../../shared/services/product-icon.service';
import { ProductResponseDTO } from '../../graphql/query-defintions/get-unlabeled-products.graphql';

@Component({
    selector: 'select-product-picker-modal',
    templateUrl: './select-product-picker-modal.component.html',
    encapsulation: ViewEncapsulation.None,
    standalone: true,
    imports: [
        ActionButtonComponent,
        MatButtonModule,
        MatDialogTitle,
        MatDialogContent,
        MatDialogActions,
        MatDialogClose,
        MatDialogModule,
        MatIconModule,
        OptionsPickerComponent,
    ]
})
export class SelectProductPickerModalComponent implements OnInit {

    dialogRef = inject(MatDialogRef<SelectProductPickerModalComponent>);
    data = inject<ProductResponseDTO[]>(MAT_DIALOG_DATA);
    productIconsService = inject(ProductIconsService);

    unitNumber = signal(null);
    products = signal<OptionPicker[]>([]);

    ngOnInit() {
        this.products.set(
            this.data
                ?.map(product => (
                    {
                        ...product,
                        statuses: [],
                        icon: this.getItemIcon(product.productFamily)
                    }
                ))
        );
    }

    getItemIcon(productFamily: string) {
        return this.productIconsService.getIconByProductFamily(productFamily);
    }

    selectOptionChange(option: OptionPicker[] | OptionPicker) {
        this.dialogRef.close(option);
    }

}
