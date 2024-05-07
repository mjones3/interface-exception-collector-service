import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatNativeDateModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import {
  ControlErrorComponent,
  ControlErrorsDirective, getAppInitializerMockProvider,
  ModalTemplateComponent,
  PatientService,
  toasterMockProvider,
  ValidationPipe,
} from '@rsa/commons';
import { PATIENT_TYPE } from '@rsa/distribution/core/models/imports.models';
import { addRsaIconsMock } from '@rsa/distribution/data/mock/icons.mock';
import { patientListMock } from '@rsa/distribution/data/mock/imports.mock';
import { createTestContext, MatDialogRefMock } from '@rsa/testing';
import { RadioButtonModule } from 'primeng/radiobutton';
import { TableModule } from 'primeng/table';
import { of } from 'rxjs';
import { PatientSearchComponent } from './patient-search.component';

describe('PatientSearchComponent', () => {
  let component: PatientSearchComponent;
  let fixture: ComponentFixture<PatientSearchComponent>;
  let patientService: PatientService;
  let matDialog: MatDialogRef<PatientSearchComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [
        PatientSearchComponent,
        ModalTemplateComponent,
        ControlErrorComponent,
        ValidationPipe,
        ControlErrorsDirective,
      ],
      imports: [
        MatDatepickerModule,
        MatInputModule,
        MatFormFieldModule,
        MatNativeDateModule,
        MatDialogModule,
        MatIconModule,
        TableModule,
        RadioButtonModule,
        ReactiveFormsModule,
        FormsModule,
        HttpClientTestingModule,
        BrowserAnimationsModule,
        TranslateModule.forRoot({
          loader: {
            provide: TranslateLoader,
            useClass: TranslateFakeLoader,
          },
        }),
      ],
      providers: [
        ...getAppInitializerMockProvider('distribution-app'),
        ...toasterMockProvider,
        ValidationPipe,
        { provide: MatDialogRef, useClass: MatDialogRefMock },
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    const testContext = createTestContext<PatientSearchComponent>(PatientSearchComponent);
    fixture = testContext.fixture;
    component = testContext.component;
    patientService = TestBed.inject(PatientService);
    matDialog = TestBed.inject(MatDialogRef);
    addRsaIconsMock(testContext);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should add new patient', () => {
    const form = {
      firstName: 'Erik',
      lastName: 'Blane',
      dob: '2000-05-23',
    };

    spyOn(patientService, 'getPatientByCriteria').and.returnValue(of([]));
    spyOn(patientService, 'createPatient').and.returnValue(of(form));

    component.patientSearch.patchValue(form);
    component.product = { bloodType: 'ABP' };
    component.searchPatient();

    expect(patientService.getPatientByCriteria).toBeCalledWith({
      type: PATIENT_TYPE,
      size: 100,
      'firstName.equals': form.firstName,
      'lastName.equals': form.lastName,
      'dob.equals': form.dob,
    });
    expect(patientService.createPatient).toBeCalledWith({
      ...form,
      deceased: false,
      abo: component.product.bloodType.substring(0, 2),
      rh: component.product.bloodType.substring(2),
      type: PATIENT_TYPE,
    });
    expect(component.patients.length).toEqual(1);
    expect(component.newPatient).toBeTruthy();
    expect(component.selectedPatient).toBeDefined();
  });

  it('should get matching patients', () => {
    const form = {
      firstName: 'john',
      lastName: 'smith',
      dob: '2021-01-29',
    };

    spyOn(patientService, 'getPatientByCriteria').and.returnValue(of(patientListMock));

    component.patientSearch.patchValue(form);
    component.product = { patient: { abo: 'AB', rh: 'P' } };
    component.searchPatient();

    expect(patientService.getPatientByCriteria).toBeCalledWith({
      type: PATIENT_TYPE,
      size: 100,
      'firstName.equals': form.firstName,
      'lastName.equals': form.lastName,
      'dob.equals': form.dob,
    });
    expect(component.patients.length).toEqual(patientListMock.length);
    expect(component.newPatient).toBeFalsy();
    expect(component.selectedPatient).toBeNull();
  });

  it('should remove patient', () => {
    const patient = patientListMock[0];

    spyOn(patientService, 'deletePatient').and.returnValue(of(patient));

    component.patients = [patient];
    component.removePatient(patient.id);

    expect(component.patients.length).toEqual(0);
    expect(component.newPatient).toBeFalsy();
  });

  it('should return patient id and close modal', () => {
    const patient = patientListMock[0];

    spyOn(matDialog, 'close');

    component.patients = [patient];
    component.save();

    expect(matDialog.close).toBeCalledWith(patient);
  });
});
