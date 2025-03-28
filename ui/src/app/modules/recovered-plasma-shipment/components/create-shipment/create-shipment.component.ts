import { CommonModule, formatDate } from '@angular/common';
import { Component, Inject, LOCALE_ID, OnInit, OnDestroy } from '@angular/core';
import {
    FormBuilder,
    FormGroup,
    ReactiveFormsModule,
    Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import {
    MatDialogActions,
    MatDialogModule,
    MatDialogRef,
} from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { Router } from '@angular/router';
import { BasicButtonComponent } from 'app/shared/components/buttons/basic-button.component';
import { SearchSelectComponent } from 'app/shared/components/search-select/search-select.component';
import { cartonWeightValidator } from 'app/shared/forms/biopro-validators';
import { OptionDTO } from 'app/shared/models/option.dto';
import { map } from 'lodash-es';
import { ToastrService } from 'ngx-toastr';
import { Subscription } from 'rxjs';

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
        MatDialogModule,
        MatDialogActions,
        BasicButtonComponent,
        SearchSelectComponent,
    ],
    templateUrl: './create-shipment.component.html',
})
export class CreateShipmentComponent implements OnInit, OnDestroy {
    //Remove Mock Data
    customerList: OptionDTO[] = [
        { code: '1', name: 'abc' },
        { code: '2', name: 'xyz' },
    ];
    //Remove Mock Data
    productTypeMap = {
        '1': [{ id: '1', value: 'aphresis' }],
        '2': [
            { id: '2', value: 'plasma' },
            { id: '3', value: 'redblood' },
        ],
    };
    createShipment: FormGroup;
    customerValueChange: Subscription;
    minDate = new Date();
    customerOption: OptionDTO[];
    productTypeOption: OptionDTO[] = [];

    constructor(
        private fb: FormBuilder,
        public dialogRef: MatDialogRef<CreateShipmentComponent>,
        private router: Router,
        private toastr: ToastrService,
        @Inject(LOCALE_ID) public locale: string
    ) {
        this.createShipment = this.fb.group({
            customerName: ['', [Validators.required]],
            productType: [{ value: '', disabled: true }, [Validators.required]],
            cartonTareWeight: [
                '',
                [Validators.required, cartonWeightValidator()],
            ],
            scheduledShipmentDate: ['', [Validators.required]],
            transportationReferenceNumber: [''],
        });
        this.customerValueChange = this.createShipment
            .get('customerName')
            .valueChanges.subscribe((value) => {
                if (value) {
                    this.createShipment.get('productType').enable();
                    this.productTypeOption = map(
                        this.productTypeMap[value],
                        (item) => ({ code: item.id, name: item.value })
                    );
                    this.createShipment.get('productType').setValue('');
                } else {
                    this.createShipment.get('productType').disable();
                    this.createShipment.get('productType').setValue('');
                    this.productTypeOption = [];
                }
            });
    }

    ngOnInit(): void {
        this.customerOption = this.customerList;
    }

    ngOnDestroy() {
        this.customerValueChange?.unsubscribe();
    }

    get createShipmentControl() {
        return this.createShipment.controls;
    }

    submit() {
        if (!this.createShipment.valid) {
            return;
        }
        const scheduledShipmentDate =
            this.createShipmentControl.scheduledShipmentDate.value;
        const formattedScheduledShipmentDate = formatDate(
            scheduledShipmentDate,
            'yyyy-MM-dd',
            this.locale
        );
        const createShipmentInfo = {
            customer: this.createShipmentControl.customerName?.value ?? '',
            productType: this.createShipmentControl.productType?.value ?? '',
            cartonTareWeight:
                this.createShipmentControl.cartonTareWeight?.value ?? '',
            scheduledTransferDate: formattedScheduledShipmentDate,
        };
        if (this.createShipment.valid) {
            this.dialogRef.close();
            this.toastr.success('sucess');
            this.router.navigateByUrl(`recovered-plasma/shipment-details`);
        }
    }
}
