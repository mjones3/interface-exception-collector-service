import { Component, EventEmitter, Inject, inject, Output } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialogActions, MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { BasicButtonComponent } from 'app/shared/components/buttons/basic-button.component';

@Component({
  selector: 'biopro-close-shipment-dailog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatInputModule,
    MatButtonModule,
    MatDatepickerModule,
    MatDialogActions,
    BasicButtonComponent
  ],
  templateUrl: './close-shipment-dailog.component.html'
})
export class CloseShipmentDailogComponent  {
  minDate = new Date();
  readonly dialog = inject(MatDialog);
  continueFn: Function;
    
  closeShipmentForm: FormGroup;

  constructor(
    public dialogRef: MatDialogRef<CloseShipmentDailogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: {shipmentDate: Date, continueFn: Function}
  ) {
    this.closeShipmentForm = new FormGroup({
      shipmentDate: new FormControl(data.shipmentDate, [Validators.required])
    });
    this.continueFn = data.continueFn;
  }
  
  onClickContinue(){    
    const formControl = this.closeShipmentForm.controls;
    const res = formControl.shipmentDate?.value ?? '';
    this.dialogRef.close();
    this.continueFn(res);
  }
}
