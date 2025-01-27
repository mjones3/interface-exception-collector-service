import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButton } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import {
    MAT_DIALOG_DATA,
    MatDialogActions,
    MatDialogClose,
    MatDialogContent,
    MatDialogRef,
    MatDialogTitle,
} from '@angular/material/dialog';
import {
    MatFormField,
    MatFormFieldModule,
    MatLabel,
} from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { CompleteOrderCommandDTO } from '../graphql/mutation-definitions/complete-order.graphql';

@Component({
    selector: 'biopro-complete-order',
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
        MatFormFieldModule,
        ReactiveFormsModule,
        MatButtonToggleModule,
    ],
    templateUrl: './complete-order.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CompleteOrderComponent {
    dialogRef = inject(
        MatDialogRef<CompleteOrderCommandDTO, CompleteOrderCommandDTO>
    );
    formBuilder = inject(FormBuilder);
    readonly commentsMaxLength = 1000;

    form = this.formBuilder.group({
        comments: ['', [Validators.maxLength(this.commentsMaxLength)]],
        createBackOrder: [false],
    });
    data = inject<{ isBackOrderCreationActive: boolean }>(MAT_DIALOG_DATA);

    continue(_event: Event): void {
        this.dialogRef.close(this.form.getRawValue());
    }
}
