import { Component, EventEmitter, Inject, inject, OnInit, Output } from '@angular/core';
import { AbstractControl, FormControl, FormGroup, ReactiveFormsModule, ValidatorFn, Validators } from '@angular/forms';
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
export class CloseShipmentDailogComponent implements OnInit  {
  minDate = new Date();
  readonly dialog = inject(MatDialog);
  continueFn: Function;
    
  shipmentDate = new FormControl('', [Validators.required])


  constructor(
    public dialogRef: MatDialogRef<CloseShipmentDailogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: {shipmentDate: string, continueFn: Function}
  ) {
    this.continueFn = data.continueFn;
  }

  ngOnInit(): void {
    if(this.data.shipmentDate){
      this.shipmentDate.setValue(this.data.shipmentDate);
      this.shipmentDate.markAsTouched();
    }
  }

  pastDateValidator = (control: FormControl) => {
    const selectedDate = new Date(control.value);
    const today = this.minDate;
    if(selectedDate > today){
      return {pastDate: true}
    }
    return null;
  }
  
  
  onClickContinue(){    
    const res = this.shipmentDate?.value ?? '';
    this.dialogRef.close();
    this.continueFn(res);
  }
}
