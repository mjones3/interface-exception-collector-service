import { AsyncPipe, LowerCasePipe, TitleCasePipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import {
    FormBuilder,
    FormControl,
    FormGroup,
    ReactiveFormsModule,
    Validators,
} from '@angular/forms';
import {
    MatDialogActions,
    MatDialogClose,
    MatDialogContent,
    MatDialogRef,
    MatDialogTitle,
} from '@angular/material/dialog';
import { MatFormField, MatLabel } from '@angular/material/form-field';
import { MatGridList, MatGridTile } from '@angular/material/grid-list';
import { MatInput } from '@angular/material/input';

import { Observable, map } from 'rxjs';

import {IrradiationService} from "../../services/irradiation.service";
import {ActionButtonComponent} from "../../../../shared/components/buttons/action-button.component";
import {ReasonDTO, RecordVisualInpectionResult} from "../../models/model";

const OTHER_REASON_KEY = 'OTHER';

@Component({
    selector: 'biopro-record-visual-inspection-modal',
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
        LowerCasePipe,
        TitleCasePipe,
    ],
    templateUrl: './record-visual-inspection-modal.component.html',
})
export class RecordVisualInspectionModalComponent implements OnInit {
    form: FormGroup;

    constructor(
        private readonly irradiationService: IrradiationService,
        private readonly formBuilder: FormBuilder,
        private readonly dialog: MatDialogRef<
            RecordVisualInspectionModalComponent,
            RecordVisualInpectionResult
        >
    ) {
        this.form = formBuilder.group<{
            visualInspection: FormControl<boolean | null>;
            reasons: FormControl<ReasonDTO[]>;

        }>({
            visualInspection: new FormControl(null, {
                validators: Validators.required,
            }),
            reasons: new FormControl([])
        });
    }

    ngOnInit(): void {

    }

    toggleButton(reason: ReasonDTO) {
        const reasons = this.reasons.value as ReasonDTO[];
        if (reasons.includes(reason)) {
            const index = reasons.indexOf(reason);
            reasons.splice(index, 1);
            this.reasons.setValue(reasons);
        } else {
            reasons.push(reason);
            this.reasons.setValue(reasons);
        }
    }

    submit() {
        const { visualInspection, comments } = this.form.value;
        this.dialog.close({
            successful: visualInspection,
            comment: comments,
            reasons: visualInspection ? [] : this.reasons.value,
        });
    }

    set visualInspection(value: boolean) {
        if (!value) {
            this.reasons.setValidators(Validators.required);
            this.reasons.updateValueAndValidity();
        } else {
            this.reasons.clearValidators();
            this.reasons.updateValueAndValidity();
        }
        this.form.controls['visualInspection'].setValue(value);
    }

    get visualInspection() {
        return this.form.controls['visualInspection'].value;
    }

    get reasons() {
        return this.form.controls['reasons'];
    }

}
