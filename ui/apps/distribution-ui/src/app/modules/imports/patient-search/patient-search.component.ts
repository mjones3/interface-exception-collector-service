import { Component, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, NgForm, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import {
  DateValidator,
  PatientDto,
  PatientService,
  ValidationPipe,
  ValidationType,
  WhiteSpaceValidator,
} from '@rsa/commons';
import { ImportItem, Patient, PATIENT_TYPE } from '@rsa/distribution/core/models/imports.models';
import * as moment from 'moment';
import { ToastrService } from 'ngx-toastr';
import { of } from 'rxjs';
import { catchError, finalize, switchMap } from 'rxjs/operators';

@Component({
  selector: 'rsa-patient-search',
  templateUrl: './patient-search.component.html',
})
export class PatientSearchComponent implements OnInit {
  readonly validationType = ValidationType;
  readonly FIRST_NAME_MAX_LENGTH = 50; //database max length value
  readonly LAST_NAME_MAX_LENGTH = 50; //database max length value

  @ViewChild('patientSearchForm') patientSearchForm: NgForm;

  patientSearch: FormGroup;
  patients: Patient[] = [];
  newPatient: boolean;
  product: ImportItem;
  loading = false;

  constructor(
    protected fb: FormBuilder,
    private patientService: PatientService,
    private matDialogRef: MatDialogRef<PatientSearchComponent>,
    private validationPipe: ValidationPipe,
    private toaster: ToastrService
  ) {
    this.patientSearch = fb.group({
      firstName: [
        '',
        [Validators.required, Validators.maxLength(this.FIRST_NAME_MAX_LENGTH), WhiteSpaceValidator.validate],
      ],
      lastName: [
        '',
        [Validators.required, Validators.maxLength(this.LAST_NAME_MAX_LENGTH), WhiteSpaceValidator.validate],
      ],
      dob: ['', [Validators.required, DateValidator.isOutOfRange(this.today)]],
    });
  }

  ngOnInit(): void {}

  save() {
    this.matDialogRef.close(this.selectedPatient);
  }

  searchPatient() {
    if (this.patientSearch.valid) {
      this.resetPatientSearch();
      this.loading = true;
      const formValues = this.patientSearch.value;
      formValues.firstName = formValues.firstName.trim();
      formValues.lastName = formValues.lastName.trim();

      this.patientService
        .getPatientByCriteria({
          type: PATIENT_TYPE,
          size: 100,
          'firstName.equals': formValues.firstName,
          'lastName.equals': formValues.lastName,
          'dob.equals': moment(formValues.dob).format('YYYY-MM-DD'),
        })
        .pipe(
          finalize(() => (this.loading = false)),
          catchError(err => {
            this.toaster.error('something-went-wrong.label');
            throw err;
          }),
          switchMap(patients => {
            if (patients?.length) {
              this.patients = patients;
            } else {
              this.newPatient = true;

              const patient: PatientDto = {
                ...formValues,
                abo: this.product.bloodType?.substring(0, this.product.bloodType?.length - 1),
                rh: this.product.bloodType?.substring(this.product.bloodType?.length - 1),
                deceased: false,
                type: PATIENT_TYPE,
              };

              return this.patientService.createPatient(patient);
            }

            return of(null);
          })
        )
        .subscribe((patient: PatientDto | null) => {
          if (patient) {
            this.patients.push({
              ...formValues,
              id: patient.id,
            });
          }
          this.patientSearchForm.resetForm();
        });
    }
  }

  removePatient(id: number) {
    this.patientService
      .deletePatient(id)
      .pipe(
        catchError(err => {
          this.toaster.error('something-went-wrong.label');
          throw err;
        })
      )
      .subscribe(() => {
        this.resetPatientSearch();
      });
  }

  resetPatientSearch() {
    this.patients = [];
    this.newPatient = false;
  }

  get today() {
    return new Date();
  }

  get dobErrorMessage() {
    const dob = this.patientSearch.get('dob');
    const validationType =
      dob.hasError('matDatepickerParse') || dob.hasError('outOfDate')
        ? this.validationType.INVALID
        : this.validationType.REQUIRED;
    return this.validationPipe.transform('date-of-birth.label', validationType);
  }

  get selectedPatient() {
    return this.patients?.length === 1 ? this.patients[0] : null;
  }
}
