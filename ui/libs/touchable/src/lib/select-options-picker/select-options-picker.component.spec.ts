import { Component, ViewChild } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { MaterialModule } from '@rsa/material';
import { createTestContext, MatDialogRefMock } from '@rsa/testing';
import { ThemeModule } from '@rsa/theme';
import { FilterableDropDownComponent, OptionsPickerDialogComponent, TouchableComponentsModule } from '@rsa/touchable';
import { TreoScrollbarMock, TreoScrollbarModule } from '@treo';
import { addTestingIconsMock } from '../../shared/testing/mocks/data/icons.mock';
import { optionsMock } from '../../shared/testing/mocks/data/shared.mock';
import { donationTypesMock, donorIntentionsMock } from '../../shared/testing/mocks/touchable.mock';
import { SelectOptionsPickerComponent } from './select-options-picker.component';

/***** SelectOptionsPicker Test Wrapper Component *****/
@Component({
  template: ` <!--    Testing with Reactive Driven Form-->
    <form id="formId" [formGroup]="formGroup">
      <rsa-select-options-picker
        #soPicker
        formControlName="control"
        [selectId]="'selectId'"
        [options]="options"
        [optionsLabel]="'name'"
        [dialogTitle]="'Title'"
        [labelTitle]="'Title'"
        labelClasses="w-40"
        [placeholder]="'placeholder'"
        selectClasses="w-100"
      ></rsa-select-options-picker>
    </form>`,
})
class SelectOptionsPickerTestWrapperComponent {
  @ViewChild('soPicker', { static: false }) soPickerComponentEl: SelectOptionsPickerComponent;
  formGroup: FormGroup = new FormGroup({
    control: new FormControl(''),
  });
  options = optionsMock;
}

describe('SelectOptionsPickerTestWrapperComponent', () => {
  let component: SelectOptionsPickerTestWrapperComponent;
  let fixture: ComponentFixture<SelectOptionsPickerTestWrapperComponent>;
  // let reactiveInputEl: HTMLInputElement;
  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [SelectOptionsPickerTestWrapperComponent],
      imports: [
        ReactiveFormsModule,
        MaterialModule,
        ThemeModule,
        TouchableComponentsModule,
        NoopAnimationsModule,
        RouterTestingModule,
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
      ],
    });
    const testContext = createTestContext<SelectOptionsPickerTestWrapperComponent>(
      SelectOptionsPickerTestWrapperComponent
    );
    fixture = testContext.fixture;
    component = testContext.component;
    addTestingIconsMock(testContext);
    fixture.detectChanges();
  });

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  it('should update form control value when change select value via reactive driven form', () => {
    expect(component.formGroup.value.control).toEqual(null);
    component.formGroup.setValue({ control: component.options[0] });
    expect(component.formGroup.value.control).toEqual(component.options[0]);
  });
});

describe('SelectOptionsPickerComponent', () => {
  let component: SelectOptionsPickerComponent;
  let fixture: ComponentFixture<SelectOptionsPickerComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        imports: [
          ReactiveFormsModule,
          MaterialModule,
          NoopAnimationsModule,
          ThemeModule,
          TouchableComponentsModule,
          RouterTestingModule,
          TranslateModule.forRoot({
            loader: { provide: TranslateLoader, useClass: TranslateFakeLoader },
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
        ],
      }).overrideModule(TouchableComponentsModule, {
        remove: { imports: [TreoScrollbarModule] },
        add: { declarations: [TreoScrollbarMock] },
      });

      const testContext = createTestContext<SelectOptionsPickerComponent>(SelectOptionsPickerComponent);
      fixture = testContext.fixture;
      component = testContext.component;
      component.options = optionsMock;
      component.optionsLabel = 'name';
      addTestingIconsMock(testContext);
      fixture.detectChanges();
    })
  );

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should open "OptionsPickerDialogComponent" dialog', function () {
    component.options = donorIntentionsMock;
    component.optionsLabel = 'name';
    component.selectOptions();
    expect(component.confirmationDialog.componentInstance instanceof OptionsPickerDialogComponent).toEqual(true);
  });

  it('should open "FilterableDropDownComponent" dialog', function () {
    component.options = donationTypesMock;
    component.optionsLabel = 'name';
    component.selectOptions();
    expect(component.confirmationDialog.componentInstance instanceof FilterableDropDownComponent).toEqual(true);
  });
});
