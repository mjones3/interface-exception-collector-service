import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { BarcodeService, getAppInitializerMockProvider, RsaCommonsModule, toasterMockProvider } from '@rsa/commons';
import { MaterialModule } from '@rsa/material';
import { createTestContext } from '@rsa/testing';
import { TreoModule } from '@treo';
import { addTestingIconsMock } from '../../shared/testing/mocks/data/icons.mock';
import {
  scdSerialNumberMock,
  scdWaferLotNumberMock,
  sterileConnectionProcessMock,
  transferContainerLotNumberMock,
} from '../../shared/testing/mocks/data/shared.mock';
import { InputKeyboardComponent } from '../input-keyboard/input-keyboard.component';
import { SelectOptionsPickerComponent } from '../select-options-picker/select-options-picker.component';
import { SterileConnectionComponent } from './sterile-connection.component';

function getSatisfactoryFields() {
  return [
    {
      descriptionKey: 'Field1',
      expectedValue: 'W999990002212',
      errorKey: 'satisfactory-unit-input-error.label',
      formControl: 'form',
      iconName: '',
    },
    {
      descriptionKey: 'Field2',
      expectedValue: 'W999990002213',
      errorKey: 'satisfactory-unit-input-error.label',
      formControl: 'form1',
      iconName: '',
    },
    {
      descriptionKey: 'Field3',
      expectedValue: 'W999990002214',
      errorKey: 'satisfactory-unit-input-error.label',
      formControl: 'form1',
      iconName: '',
    },
    {
      descriptionKey: 'Field4',
      expectedValue: 'W999990002215',
      errorKey: 'satisfactory-unit-input-error.label',
      formControl: 'form1',
      iconName: '',
    },
    {
      descriptionKey: 'Field5',
      expectedValue: 'W999990002216',
      errorKey: 'satisfactory-unit-input-error.label',
      formControl: 'form1',
      iconName: '',
    },
  ];
}

describe('SterileConnectionComponent', () => {
  let component: SterileConnectionComponent;
  let fixture: ComponentFixture<SterileConnectionComponent>;

  let barcodeService: BarcodeService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [SterileConnectionComponent, InputKeyboardComponent, SelectOptionsPickerComponent],
      imports: [
        NoopAnimationsModule,
        MaterialModule,
        TreoModule,
        RouterTestingModule,
        RsaCommonsModule,
        MatButtonToggleModule,
        HttpClientTestingModule,
        TranslateModule.forRoot({
          loader: {
            provide: TranslateLoader,
            useClass: TranslateFakeLoader,
          },
        }),
      ],
      providers: [...toasterMockProvider, ...getAppInitializerMockProvider('manufacturing-app')],
    });
    const testContext = createTestContext<SterileConnectionComponent>(SterileConnectionComponent);
    fixture = testContext.fixture;
    component = testContext.component;
    component.sterileConnectionProcess = sterileConnectionProcessMock;
    component.scdSerialNumber = scdSerialNumberMock;
    component.scdWaferLotNumber = scdWaferLotNumberMock;
    component.transferContainerLotNumber = transferContainerLotNumberMock;
    component.satisfactoryFields = [
      {
        descriptionKey: 'Field1',
        expectedValue: 'W999990002212',
        errorKey: 'satisfactory-unit-input-error.label',
        formControl: 'form',
        iconName: '',
      },
    ];

    barcodeService = TestBed.inject(BarcodeService);

    addTestingIconsMock(testContext);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should add form validations for weld Inspections satisfactory', () => {
    component.isWeldInspectionSatisfactory = true;
    component.satisfactoryFields = getSatisfactoryFields();
    component.addFormValidationsForWeldInspections();
    expect(component.weldInspectionForm.contains('satisfactory')).toEqual(true);
    expect(component.weldInspectionForm.contains('unsatisfactory')).not.toEqual(true);
  });

  it('should add form validations for weld Inspections unsatisfactory', () => {
    component.isWeldInspectionSatisfactory = false;
    component.addFormValidationsForWeldInspections();
    expect(component.weldInspectionForm.contains('satisfactory')).not.toEqual(true);
    expect(component.weldInspectionForm.contains('unsatisfactory')).toEqual(true);
  });

  it('should reset Inspection Values', () => {
    component.weldInspectionForm.controls['inspection'].setValue(true);
    fixture.detectChanges();
    expect(component.isWeldInspectionSatisfactory).toEqual(true);
    expect(component.isReweldActionSelected).toEqual(undefined);
    expect(component.weldInspectionForm.contains('satisfactory')).toEqual(true);
    expect(component.weldInspectionForm.contains('unsatisfactory')).not.toEqual(true);
  });

  it('should emit form validity', () => {
    spyOn(component.step1FormValidity, 'emit').and.callThrough();
    component.sterileConnectionForm.patchValue({ scdsn: '' });
    expect(component.step1FormValidity.emit).toHaveBeenCalledWith(false);
  });
});
