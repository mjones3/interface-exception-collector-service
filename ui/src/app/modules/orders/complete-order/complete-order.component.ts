import { Component, inject } from '@angular/core';
import { CompleteOrderCommandDTO } from '../graphql/mutation-definitions/complete-order.graphql';
import {
    MatDialogActions,
    MatDialogClose,
    MatDialogContent,
    MatDialogRef,
    MatDialogTitle,
} from '@angular/material/dialog';
import { MatFormField, MatLabel } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { MatButton } from '@angular/material/button';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

@Component({
    selector: 'app-complete-order',
    standalone: true,
    imports: [
        MatDialogTitle,
        MatDialogContent,
        MatLabel,
        MatFormField,
        MatInput,
        MatDialogActions,
        MatButton,
        MatDialogClose,
        ReactiveFormsModule,
    ],
    templateUrl: './complete-order.component.html',
})
export class CompleteOrderComponent {
    dialogRef = inject(MatDialogRef<CompleteOrderCommandDTO, CompleteOrderCommandDTO>);
    formBuilder = inject(FormBuilder);

    form = this.formBuilder.group({
        comments: ['', [ Validators.maxLength(1000) ]],
    });

    continue(_event: Event): void {
        this.dialogRef.close(this.form.getRawValue());
    }
}
