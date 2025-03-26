import { CommonModule, formatDate } from '@angular/common';
import { Component, Inject, LOCALE_ID, OnInit } from '@angular/core';
import {
    FormBuilder,
    FormGroup,
    ReactiveFormsModule,
    Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import {
    MAT_DIALOG_DATA,
    MatDialogActions,
    MatDialogRef,
} from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { ActionButtonComponent } from 'app/shared/components/buttons/action-button.component';
import { SearchSelectComponent } from 'app/shared/components/search-select/search-select.component';
import { CustomerOptionDTO } from 'app/shared/models/customer-option.dto';

@Component({
    selector: 'biopro-create-shipment',
    standalone: true,
    imports: [
        ReactiveFormsModule,
        MatInputModule,
        CommonModule,
        MatIconModule,
        MatSelectModule,
        MatButtonModule,
        MatDatepickerModule,
        ActionButtonComponent,
        MatDialogActions,
        SearchSelectComponent,
    ],
    templateUrl: './create-shipment.component.html',
})
export class CreateShipmentComponent implements OnInit {
    createShipment: FormGroup;
    minDate = new Date();
    customerOption: CustomerOptionDTO[] = [];
    productTypeOption: CustomerOptionDTO[] = [];

    constructor(
        public dialogRef: MatDialogRef<CreateShipmentComponent>,
        @Inject(MAT_DIALOG_DATA)
        public data: {
            customer: CustomerOptionDTO[];
            productType: CustomerOptionDTO;
        },
        @Inject(LOCALE_ID) public locale: string,
        private fb: FormBuilder
    ) {
        this.createShipment = this.fb.group({
            customerName: ['', [Validators.required]],
            productType: [{ value: '', disabled: true }, [Validators.required]],
            cartonTareWeight: ['', [Validators.required]],
            sheduledShipmentDate: ['', [Validators.required]],
            transportationReferenceNumber: [''],
        });
    }

    ngOnInit(): void {
        this.customerOption = this.data.customer || [];
    }

    get createShipmentControl() {
        return this.createShipment.controls;
    }

    //TODO
    submit() {
        const scheduleTransferDate =
            this.createShipmentControl.scheduleTransferDate.value;
        const formattedscheduleShipmentDate = formatDate(
            scheduleTransferDate,
            'yyyy-MM-dd',
            this.locale
        );
        const createShipmentInfo = {
            customer: this.createShipmentControl.customer?.value ?? '',
            productType: this.createShipmentControl.productType?.value ?? '',
            cartonTareWeight:
                this.createShipmentControl.cartonTareWeight?.value ?? '',
            scheduledTransferDate: formattedscheduleShipmentDate,
        };
        if (this.createShipment.valid) {
            const shipmentInfo = createShipmentInfo;
            console.log('createshipment', shipmentInfo);
        }
    }
}
