import { Component, ViewChild } from '@angular/core';
import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { By } from '@angular/platform-browser';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { IconService, Option, Orientation } from '@rsa/commons';
import { createTestContext, MatDialogRefMock } from '@rsa/testing';
import { ProductPlasmaRsa, ProductPlateletsRsa, ProductRbcRsa, ThemeModule } from '@rsa/theme';
import { OptionsPickerComponent } from '@rsa/touchable';
import { TreoCardModule } from '@treo';
import { addTestingIconsMock } from '../../shared/testing/mocks/data/icons.mock';
import { optionsMock } from '../../shared/testing/mocks/data/shared.mock';

const RSA_ICONS = [ProductPlasmaRsa, ProductPlateletsRsa, ProductRbcRsa];

/***** Test Wrapper Component *****/
@Component({
  template: `
    <!--    Testing with Reactive Driven Form-->
    <form id="form1Id" [formGroup]="formGroup">
      <rsa-options-picker
        #opComponentEl
        formControlName="control"
        headerTitle="Options"
        [options]="options"
      ></rsa-options-picker>
    </form>
    <!--    Testing with Template Driven Form-->
    <form id="form2Id">
      <rsa-options-picker
        #opComponentElNgModel
        [(ngModel)]="controlNgModel"
        headerTitle="Options"
        name="control"
        [options]="options"
      ></rsa-options-picker>
    </form>
  `,
})
class OptionsPickerTestWrapperComponent {
  @ViewChild('opComponentEl', { static: false }) opComponentEl: OptionsPickerComponent;
  @ViewChild('opComponentElNgModel', { static: false }) opComponentElNgModel: OptionsPickerComponent;
  formGroup: FormGroup = new FormGroup({
    control: new FormControl(''),
  });
  controlNgModel = '';
  options: Option[] = [
    { name: 'Option 1', selectionKey: '1', icon: 'product-platelets' },
    { name: 'Option 2', selectionKey: '2', icon: 'product-plasma' },
  ];

  constructor(private iconService: IconService) {
    iconService.addIcon(...RSA_ICONS);
  }
}

describe('OptionsPickerTestWrapperComponent', () => {
  let component: OptionsPickerTestWrapperComponent;
  let fixture: ComponentFixture<OptionsPickerTestWrapperComponent>;
  let reactiveOptionEl: HTMLButtonElement;
  let ngModelOptionEl: HTMLInputElement;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [OptionsPickerTestWrapperComponent, OptionsPickerComponent],
      imports: [
        ReactiveFormsModule,
        NoopAnimationsModule,
        TreoCardModule,
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
            buttons: optionsMock,
          },
        },
        { provide: MatDialogRef, useClass: MatDialogRefMock },
        IconService,
      ],
    });
    const testContext = createTestContext<OptionsPickerTestWrapperComponent>(OptionsPickerTestWrapperComponent);
    fixture = testContext.fixture;
    component = testContext.component;
    addTestingIconsMock(testContext);
    fixture.detectChanges();
    reactiveOptionEl = fixture.debugElement.query(By.css('#form1Id button:nth-of-type(2)')).nativeElement;
    ngModelOptionEl = fixture.debugElement.query(By.css('#form2Id button:nth-of-type(1)')).nativeElement;
  });

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  it('should update form control value when change value in the component via reactive driven form', () => {
    const value = { name: 'Option 2', selectionKey: '2', icon: 'product-plasma' };
    expect(component.formGroup.value.control).toEqual('');
    reactiveOptionEl.click();
    fixture.detectChanges();
    expect(component.formGroup.value.control.name).toEqual(value.name);
    expect(component.formGroup.value.control.selectionKey).toEqual(value.selectionKey);
  });

  it('should update ngModel value when change value in component via template driven form', () => {
    const value = { name: 'Option 1', selectionKey: '1', icon: 'product-platelets' };
    expect(component.controlNgModel).toEqual('');
    ngModelOptionEl.click();
    fixture.detectChanges();
    expect(component.controlNgModel).toEqual(value);
  });

  it('should fill form component value when change value via reactive driven form', () => {
    component.formGroup.patchValue({ control: '1' });
    fixture.detectChanges();
    expect(component.opComponentEl.value).toEqual('1');
  });

  it('should fill form component ' + 'value when change value via template driven form', done => {
    fakeAsync(() => {
      const value = '1';
      component.controlNgModel = value;
      fixture.detectChanges();
      tick();
      expect(component.opComponentElNgModel.value).toEqual(value);
      done();
    })();
  });

  it('should deselect a selected item', () => {
    component.opComponentEl.multiple = true;
    fixture.detectChanges();
    component.opComponentEl.selectOption(component.options[0]);
    expect(component.opComponentEl.selectedOptions.selected.length).toEqual(1);
    component.opComponentEl.deselectOption(component.options[0]);
    expect(component.opComponentEl.selectedOptions.selected.length).toEqual(0);
  });

  it('should deselect multiple selected items', () => {
    component.opComponentEl.multiple = true;
    component.opComponentEl.ngOnInit();
    fixture.detectChanges();
    component.opComponentEl.selectOption(component.options);
    expect(component.opComponentEl.selectedOptions.selected.length).toEqual(2);
    component.opComponentEl.deselectOption(component.options);
    expect(component.opComponentEl.selectedOptions.selected.length).toEqual(0);
  });

  it('should deselect multiple items', () => {
    component.opComponentEl.multiple = true;
    component.opComponentEl.ngOnInit();
    fixture.detectChanges();
    component.opComponentEl.selectOption(component.options);
    expect(component.opComponentEl.selectedOptions.selected.length).toEqual(2);
  });

  it('should change orientation to rows', () => {
    component.opComponentEl.orientation = Orientation.ROW;
    component.opComponentEl.ngOnInit();
    fixture.detectChanges();
    expect(component.opComponentEl.columns).toEqual(2);
  });

  it('should have ready to label/relabel inside product options', () => {
    const option = {
      label: 'ready-to-label.label',
      descriptionKey: '',
      inventoryId: 2000000027176,
      relabel: false,
      icon: '',
    };
    component.opComponentEl.optionsLabel = ['label'];
    const buttonLabel = component.opComponentEl.getOptionLabel(option);
    expect(buttonLabel).toEqual('ready-to-label.label');
  });
});
