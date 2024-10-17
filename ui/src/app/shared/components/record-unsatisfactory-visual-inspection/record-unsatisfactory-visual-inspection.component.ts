import { AsyncPipe, TitleCasePipe, UpperCasePipe } from '@angular/common';
import { Component, Inject, OnDestroy, signal } from '@angular/core';
import {
    FormBuilder,
    FormControl,
    FormGroup,
    ReactiveFormsModule,
    Validators,
} from '@angular/forms';
import {
    MAT_DIALOG_DATA,
    MatDialogActions,
    MatDialogClose,
    MatDialogContent,
    MatDialogRef,
    MatDialogTitle,
} from '@angular/material/dialog';
import { MatFormField, MatLabel } from '@angular/material/form-field';
import { MatGridList, MatGridTile } from '@angular/material/grid-list';
import { MatInput } from '@angular/material/input';
import { Subscription } from 'rxjs';
import { InventoryDTO } from '../../models/inventory.model';
import { ReasonDTO, ReasonMap } from '../../models/reason.dto';
import { ActionButtonComponent } from '../action-button/action-button.component';

export interface RecordUnsatisfactoryVisualInspectionData {
    reasons: ReasonDTO[];
    message?: string;
    inventory?: InventoryDTO;
}

export interface RecordUnsatisfactoryVisualInspectionResult {
    result: 'SUBMIT' | 'CANCEL';
    reason: ReasonDTO;
    comments: string;
    message?: string;
    inventory?: InventoryDTO;
}

@Component({
    selector: 'app-record-unsatisfactory-visual-inspection',
    standalone: true,
    imports: [
        ActionButtonComponent,
        ReactiveFormsModule,
        MatDialogTitle,
        MatDialogContent,
        MatDialogActions,
        MatDialogClose,
        MatGridList,
        MatGridTile,
        MatFormField,
        MatLabel,
        MatInput,
        AsyncPipe,
        TitleCasePipe,
        UpperCasePipe,
    ],
    templateUrl: './record-unsatisfactory-visual-inspection.component.html',
})
export class RecordUnsatisfactoryVisualInspectionComponent
    implements OnDestroy
{
    dataSignal = signal<RecordUnsatisfactoryVisualInspectionData>(null);

    form: FormGroup;
    formValueChange: Subscription;

    constructor(
        @Inject(MAT_DIALOG_DATA)
        protected data: RecordUnsatisfactoryVisualInspectionData,
        protected dialogRef: MatDialogRef<
            RecordUnsatisfactoryVisualInspectionComponent,
            RecordUnsatisfactoryVisualInspectionResult
        >,
        protected readonly formBuilder: FormBuilder
    ) {
        this.dataSignal.set(data);
        this.buildFormGroup();
    }

    buildFormGroup(): void {
        const formGroup = this.formBuilder.group({
            reason: new FormControl<ReasonDTO>(null, [Validators.required]),
            comments: new FormControl<string>('', []),
        });

        this.formValueChange = formGroup.controls.reason.valueChanges.subscribe(
            (value) => {
                // Requires comments if the Reason is configured to
                value?.requireComments
                    ? formGroup.controls.comments.setValidators([
                          Validators.required,
                      ])
                    : formGroup.controls.comments.clearValidators();
                formGroup.controls.comments.updateValueAndValidity();
            }
        );

        this.form = formGroup;
    }

    ngOnDestroy(): void {
        this.formValueChange?.unsubscribe();
    }

    close(result: RecordUnsatisfactoryVisualInspectionResult['result']): void {
        this.dialogRef.close({
            result,
            inventory: this.dataSignal().inventory,
            message: this.dataSignal().message,
            ...this.form.value,
        });
    }

    protected readonly ReasonMap = ReasonMap;
}
