import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnInit,
  Output,
  ViewChild,
  ViewEncapsulation,
} from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { MatStepper } from '@angular/material/stepper';
import { BarcodeService, ValidationType } from '@rsa/commons';
import { DynamicConfigFields } from '../../shared/models/dynamic-config-fields';
import { InputType } from '../../shared/models/input-type.enum';

@Component({
  selector: 'rsa-sterile-connection',
  exportAs: 'rsaSterileConnection',
  templateUrl: './sterile-connection.component.html',
  styleUrls: ['./sterile-connection.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  encapsulation: ViewEncapsulation.None,
})
export class SterileConnectionComponent implements OnInit, AfterViewInit {
  @ViewChild('sterileConnectionStepper') stepper: MatStepper;
  @ViewChild('unitNumber', { static: true }) unitNumberInput: ElementRef;

  // Process Status
  @Input() definedProcess: string;
  @Input() sterileConnectionCompleted: boolean;
  @Input() weldInspectionCompleted: boolean;

  @Input() useBarcodeTranslationWeldInspection: boolean;

  // Sterile Connection Select Data (Step 1)
  @Input() sterileConnectionProcess: any[];
  @Input() scdSerialNumber: any[];
  @Input() scdWaferLotNumber: any[];
  @Input() transferContainerLotNumber: any[];
  @Input() currentContainerLotNumber: string;

  @Input() set currentContLotNumberDisabled(value: boolean) {
    this._currentContLotNumberDisabled = value;
  }

  // Weld Inspection Dynamic Fields
  @Input() satisfactoryFields: DynamicConfigFields[] = [];
  satisfactoryFieldsColumns1: DynamicConfigFields[] = [];
  satisfactoryFieldsColumns2: DynamicConfigFields[] = [];
  @Input() visualInspection: boolean;

  // Forms Status
  @Output() processSelected: EventEmitter<boolean> = new EventEmitter<boolean>();
  @Output() step1FormValidity: EventEmitter<boolean> = new EventEmitter<boolean>();
  @Output() step2FormValidity: EventEmitter<boolean> = new EventEmitter<boolean>();
  @Output() sterileConnectionDataForm: EventEmitter<boolean> = new EventEmitter<boolean>();

  // Forms
  sterileConnectionForm: FormGroup;
  weldInspectionForm: FormGroup;
  satisfactoryForm: FormGroup;
  unsatisfactoryForm: FormGroup;

  readonly commentsMaxLength = 2000;
  readonly inputTypeEnum = InputType;
  readonly validationType = ValidationType;

  isWeldInspectionSatisfactory: boolean;
  isReweldActionSelected: boolean;
  selectedIndex: number;
  _currentContLotNumberDisabled: boolean;

  controlValue = {};

  constructor(private formBuilder: FormBuilder, private barcodeService: BarcodeService) {}

  ngOnInit(): void {
    if (this.satisfactoryFields.length > 1) {
      this.sortSatisfactoryFields();
    }

    this.initSterileConnectionForm(); //Step 1 Form
    this.initWeldInspectionForm(); //Step 2 Form

    if (!this.sterileConnectionCompleted) {
      this.selectedIndex = 1;
    }
  }

  onTabOrEnter(event: any, controlName: string) {
    if (this.useBarcodeTranslationWeldInspection) {
      const control = this.satisfactoryForm.controls[controlName];
      const value = control.value;
      if (value) {
        if (this.controlValue[controlName] === value) {
          return;
        }

        this.controlValue[controlName] = value;
        const regex = /=[A-Z0-9]{15}/gm;
        if (regex.test(value)) {
          this.barcodeService.getBarcodeTranslation(value).subscribe(response => {
            const translations = response.body?.barcodeTranslation ?? {};
            if (translations) {
              control.reset();
              control.patchValue(translations['unitNumber']);

              this.controlValue[controlName] = translations['unitNumber'];
            }
          });
        }
      } else {
        delete this.controlValue[controlName];
      }
    }
  }

  sortSatisfactoryFields(): void {
    this.satisfactoryFields.sort((a, b) => a.orderNum - b.orderNum);
  }

  /**
   * Step1: Sterile Connection - Form Initialization
   **/
  initSterileConnectionForm(): void {
    this.sterileConnectionForm = this.formBuilder.group({
      ccln: ['', Validators.required],
    });

    if (this.currentContainerLotNumber) {
      this.sterileConnectionForm.controls['ccln'].setValue(this.currentContainerLotNumber);
    }

    this.sterileConnectionForm.addControl('scdsn', new FormControl(null, Validators.required));
    this.sterileConnectionForm.addControl('scdwln', new FormControl(null, Validators.required));
    this.sterileConnectionForm.addControl('tcln', new FormControl(null, Validators.required));

    // Field Sterile Connection Process in Step 1 will be dynamic depending of the process using it (Main Sterile Connection or other processes)
    // added input definedProcess for now, latter with process logic we can change the name or the way to identify it
    if (!this.definedProcess) {
      this.sterileConnectionForm.addControl('scp', new FormControl(this.definedProcess, Validators.required));
      this.sterileConnectionForm.controls['scp'].valueChanges.subscribe(value => {
        this.processSelected.emit(value);
      });
    }

    // Subscribe to Form Status changes
    this.sterileConnectionForm.statusChanges.subscribe(status => {
      this.step1FormValidity.emit(status === 'VALID');
      if (status === 'VALID') {
        this.sterileConnectionDataForm.emit(this.sterileConnectionForm.value);
      }
    });
  }

  /**
   * Step2: Weld Inspection - Form Initialization
   **/
  initWeldInspectionForm(): void {
    this.weldInspectionForm = this.formBuilder.group({
      inspection: ['', Validators.required],
      comments: [''],
    });

    this.weldInspectionForm.controls['inspection'].valueChanges.subscribe(value => {
      this.resetInspectionValues(value);
    });

    // Fields For Satisfactory
    this.satisfactoryForm = new FormGroup({});
    this.satisfactoryFields.forEach(field => {
      this.satisfactoryForm.addControl(
        field.formControl,
        new FormControl('', [Validators.required, Validators.pattern('(=)?' + field.expectedValue + '(00)?')])
      );
    });

    // Fields For Unsatisfactory
    this.unsatisfactoryForm = new FormGroup({
      actionTaken: new FormControl('', Validators.required),
    });

    // Subscribe to Form Status changes
    this.weldInspectionForm.statusChanges.subscribe(status => {
      this.step2FormValidity.emit(status === 'VALID');
    });
  }

  addFormValidationsForWeldInspections(): void {
    // Satisfactory
    if (this.isWeldInspectionSatisfactory) {
      this.weldInspectionForm.addControl('satisfactory', this.satisfactoryForm);
      this.weldInspectionForm.removeControl('unsatisfactory');

      if (this.satisfactoryFields.length > 2) {
        this.organizeColumns();
      } else {
        this.satisfactoryFieldsColumns1 = this.satisfactoryFields;
      }
    } else {
      // Unsatisfactory
      this.weldInspectionForm.addControl('unsatisfactory', this.unsatisfactoryForm);
      this.weldInspectionForm.removeControl('satisfactory');
    }
  }

  organizeColumns(): void {
    const slicePosition =
      this.satisfactoryFields.length % 2 === 0
        ? this.satisfactoryFields.length / 2
        : Math.floor(this.satisfactoryFields.length / 2) + 1;

    this.satisfactoryFieldsColumns1 = this.satisfactoryFields.slice(0, slicePosition);
    this.satisfactoryFieldsColumns2 = this.satisfactoryFields.slice(slicePosition, this.satisfactoryFields.length);
  }

  resetInspectionValues(value: boolean) {
    this.isWeldInspectionSatisfactory = value;
    this.isReweldActionSelected = undefined;
    this.addFormValidationsForWeldInspections();
  }

  ngAfterViewInit(): void {
    setTimeout(() => {
      if (this._currentContLotNumberDisabled) {
        this.sterileConnectionForm.get('ccln').disable();
      } else if (!this._currentContLotNumberDisabled && this.sterileConnectionForm.get('ccln').disabled) {
        this.sterileConnectionForm.get('ccln').enable();
      }
    });
  }
}
