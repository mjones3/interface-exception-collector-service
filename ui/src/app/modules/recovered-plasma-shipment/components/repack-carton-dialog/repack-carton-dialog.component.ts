import { Component, inject } from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogActions, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { BasicButtonComponent } from 'app/shared/components/buttons/basic-button.component';

@Component({
  selector: 'biopro-repack-carton',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatInputModule,
    BasicButtonComponent,
    MatFormFieldModule,
    MatDialogActions
  ],
  templateUrl: './repack-carton-dialog.component.html',
})
export class RepackCartonDialogComponent {
  readonly commentsMaxLength: number = 250;
  dialogRef = inject(MatDialogRef<RepackCartonDialogComponent>);
  reasonComments = new FormControl('', [Validators.required]);

  onClickContinue(){    
    const req = this.reasonComments?.value ?? '';
    this.dialogRef.close(req);
  }
}
