import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators,} from '@angular/forms';
import {
    MatDialogActions,
    MatDialogClose,
    MatDialogContent,
    MatDialogRef,
    MatDialogTitle,
} from '@angular/material/dialog';

import {IrradiationService} from "../../services/irradiation.service";
import {ActionButtonComponent} from "../../../../shared/components/buttons/action-button.component";
import {RecordVisualInpectionResult} from "../../models/model";

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

        }>({
            visualInspection: new FormControl(null, {
                validators: Validators.required,
            })
        });
    }

    ngOnInit(): void {

    }

    submit() {
        const { visualInspection, comments } = this.form.value;
        this.dialog.close({
            irradiated: visualInspection,
            comment: comments
        });
    }

    set visualInspection(value: boolean) {
        this.form.controls['visualInspection'].setValue(value);
    }

    get visualInspection() {
        return this.form.controls['visualInspection'].value;
    }
}
