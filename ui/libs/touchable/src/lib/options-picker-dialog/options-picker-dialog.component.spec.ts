import { Component, ViewChild } from '@angular/core';
import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { MaterialModule } from '@rsa/material';
import { createTestContext, MatDialogRefMock } from '@rsa/testing';
import { ThemeModule } from '@rsa/theme';
import { OptionsPickerDialogComponent } from '@rsa/touchable';
import { addTestingIconsMock } from '../../shared/testing/mocks/data/icons.mock';
import { optionsMock } from '../../shared/testing/mocks/data/shared.mock';
import { IconService } from '@rsa/commons';

const pickerOptions = [
  { name: 'Option 1', selectionKey: '1', icon: 'product-platelets' },
  { name: 'Option 2', selectionKey: '2', icon: 'product-platelets' },
];

/***** Test Wrapper Component *****/
@Component({
  template: `
    <!--    Testing with Reactive Driven Form-->
    <form id="form1Id" [formGroup]="formGroup">
      <rsa-options-picker-dialog
        #opComponentEl
        formControlName="control"
        headerTitle="Options"
        [options]="options"
      ></rsa-options-picker-dialog>
    </form>
    <!--    Testing with Template Driven Form-->
    <form id="form2Id">
      <rsa-options-picker-dialog
        #opComponentElNgModel
        [(ngModel)]="controlNgModel"
        headerTitle="Options"
        name="control"
        [options]="options"
      ></rsa-options-picker-dialog>
    </form>
  `,
})
class OptionsPickerDialogTestWrapperComponent {
  @ViewChild('opComponentEl', { static: false }) opComponentEl: OptionsPickerDialogComponent;
  @ViewChild('opComponentElNgModel', { static: false }) opComponentElNgModel: OptionsPickerDialogComponent;
  formGroup: FormGroup = new FormGroup({
    control: new FormControl(''),
  });
  controlNgModel = '';
  options = [...pickerOptions];
}

describe('OptionsPickerDialogTestWrapperComponent', () => {
  let component: OptionsPickerDialogTestWrapperComponent;
  let fixture: ComponentFixture<OptionsPickerDialogTestWrapperComponent>;
  let reactiveOptionEl: HTMLButtonElement;
  let ngModelOptionEl: HTMLButtonElement;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [OptionsPickerDialogTestWrapperComponent, OptionsPickerDialogComponent],
      imports: [
        ReactiveFormsModule,
        MaterialModule,
        NoopAnimationsModule,
        ThemeModule,
        TranslateModule.forRoot({
          loader: {
            provide: TranslateLoader,
            useClass: TranslateFakeLoader,
          },
        }),
      ],
      providers: [
        {
          provide: MAT_DIALOG_DATA,
          useValue: {
            iconName: 'Name',
            dialogTitle: 'Title',
            dialogText: 'Dialog Text',
            options: optionsMock,
          },
        },
        { provide: MatDialogRef, useClass: MatDialogRefMock },
        IconService,
      ],
    });
    const testContext = createTestContext<OptionsPickerDialogTestWrapperComponent>(
      OptionsPickerDialogTestWrapperComponent
    );
    fixture = testContext.fixture;
    component = testContext.component;
    addTestingIconsMock(testContext);
    fixture.detectChanges();
    reactiveOptionEl = testContext.getElByCss('#form1Id button.rounded.option-button:nth-of-type(2)');
    ngModelOptionEl = testContext.getElByCss('#form2Id button.rounded.option-button:nth-of-type(1)');
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should update form control value when change value in textfield via reactive driven form', () => {
    const value = '2';
    expect(component.formGroup.value.control).toEqual('');
    reactiveOptionEl.click();
    fixture.detectChanges();
    expect(component.formGroup.value.control).toEqual(value);
  });

  it('should update ngModel value when change value in textfield via template driven form', () => {
    const value = '1';
    expect(component.controlNgModel).toEqual('');
    ngModelOptionEl.click();
    fixture.detectChanges();
    expect(component.controlNgModel).toEqual(value);
  });

  it('should fill form value when change value via reactive driven form', () => {
    component.formGroup.patchValue({ control: '1' });
    fixture.detectChanges();
    expect(component.opComponentEl.value).toEqual('1');
  });

  it('should fill form value when change value via template driven form', done => {
    fakeAsync(() => {
      const value = '1';
      component.controlNgModel = value;
      fixture.detectChanges();
      tick();
      expect(component.opComponentElNgModel.value).toEqual(value);
      done();
    })();
  });
});

describe('OptionsConfirmationDialogComponent', () => {
  let component: OptionsPickerDialogComponent;
  let fixture: ComponentFixture<OptionsPickerDialogComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [OptionsPickerDialogComponent],
      imports: [
        MaterialModule,
        TranslateModule.forRoot({
          loader: {
            provide: TranslateLoader,
            useClass: TranslateFakeLoader,
          },
        }),
      ],
      providers: [
        { provide: MAT_DIALOG_DATA, useValue: {} },
        {
          provide: MatDialogRef,
          useValue: {
            close(value?: any) {},
          },
        },
      ],
    });
    const testContext = createTestContext<OptionsPickerDialogComponent>(OptionsPickerDialogComponent);
    fixture = testContext.fixture;
    component = testContext.component;
    component.options = [
      { name: 'Option 1', selectionKey: '1', icon: 'product-platelets' },
      { name: 'Option 2', selectionKey: '2', icon: 'product-platelets' },
    ];
    addTestingIconsMock(testContext);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should select an option', () => {
    spyOn(component.optionChange, 'emit').and.callThrough();
    component.selectOption(component.options[0]);
    expect(component.optionChange.emit).toHaveBeenCalledWith(component.options[0]);
  });
});
