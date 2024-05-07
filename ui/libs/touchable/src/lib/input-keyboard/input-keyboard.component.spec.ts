import { Component, ViewChild } from '@angular/core';
import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { RsaCommonsModule } from '@rsa/commons';
import { MaterialModule } from '@rsa/material';
import { createTestContext, TestContext, typeInElement } from '@rsa/testing';
import { InputKeyboardComponent, TouchableComponentsModule } from '@rsa/touchable';
import 'jest-canvas-mock';
import { addTestingIconsMock } from '../../shared/testing/mocks/data/icons.mock';

/***** Test Wrapper Component *****/
@Component({
  template: ` <!--    Testing with Reactive Driven Form-->
    <form id="form1Id" [formGroup]="formGroup">
      <rsa-input-keyboard #ikComponentEl class="form-control" formControlName="control"></rsa-input-keyboard>
    </form>
    <!--    Testing with Template Driven Form-->
    <form id="form2Id">
      <rsa-input-keyboard
        #ikComponentElNgModel
        class="form-control"
        [(ngModel)]="controlNgModel"
        name="control"
      ></rsa-input-keyboard>
    </form>`,
})
class InputKeyboardTestWrapperComponent {
  @ViewChild('ikComponentEl', { static: false }) ikComponentEl: InputKeyboardComponent;
  @ViewChild('ikComponentElNgModel', { static: false }) ikComponentElNgModel: InputKeyboardComponent;
  formGroup: FormGroup = new FormGroup({
    control: new FormControl(''),
  });
  controlNgModel = '';
}

describe('InputKeyboardTestWrapperComponent', () => {
  let component: InputKeyboardTestWrapperComponent;
  let fixture: ComponentFixture<InputKeyboardTestWrapperComponent>;
  let reactiveInputEl: HTMLInputElement;
  let ngModelInputEl: HTMLInputElement;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [InputKeyboardTestWrapperComponent, InputKeyboardComponent],
      imports: [
        ReactiveFormsModule,
        MaterialModule,
        RsaCommonsModule,
        NoopAnimationsModule,
        TranslateModule.forRoot({
          loader: {
            provide: TranslateLoader,
            useClass: TranslateFakeLoader,
          },
        }),
      ],
    });

    const testContext = createTestContext<InputKeyboardTestWrapperComponent>(InputKeyboardTestWrapperComponent);
    fixture = testContext.fixture;
    component = testContext.component;
    addTestingIconsMock(testContext);
    fixture.detectChanges();
    reactiveInputEl = testContext.getElByCss('#form1Id input[formControlName=\'input\']');
    ngModelInputEl = testContext.getElByCss('#form2Id input[formControlName=\'input\']');
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should update form control value when change value in textfield via reactive driven form', () => {
    const value = 'Reactive value';
    expect(component.formGroup.value.control).toEqual('');
    reactiveInputEl.value = value;
    reactiveInputEl.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    expect(component.formGroup.value.control).toEqual(value);
  });

  it('should update ngModel value when change value in textfield via template driven form', () => {
    const value = 'Template value';
    expect(component.controlNgModel).toEqual('');
    ngModelInputEl.value = value;
    ngModelInputEl.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    expect(component.controlNgModel).toEqual(value);
  });

  it('should fill form value when change value via reactive driven form', () => {
    const value = 'Reactive value';
    component.formGroup.patchValue({ control: value });
    fixture.detectChanges();
    expect(component.ikComponentEl.value).toEqual(value);
  });

  //TODO: Fix unit test
  it.skip('should fill form value when change value via template driven form', done => {
    fakeAsync(() => {
      const value = 'Template value';
      component.controlNgModel = value;
      fixture.detectChanges();
      tick();
      expect(component.ikComponentElNgModel.value).toEqual(value);
      done();
    })();
  });
});

describe('InputKeyboardComponent', () => {
  let component: InputKeyboardComponent;
  let fixture: ComponentFixture<InputKeyboardComponent>;
  let keyboardButton: HTMLButtonElement;
  let testContext: TestContext<InputKeyboardComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        TouchableComponentsModule,
        RouterTestingModule,
        ReactiveFormsModule,
        MaterialModule,
        NoopAnimationsModule,
        RsaCommonsModule,
        TranslateModule.forRoot({
          loader: {
            provide: TranslateLoader,
            useClass: TranslateFakeLoader,
          },
        }),
      ],
    });

    testContext = createTestContext<InputKeyboardComponent>(InputKeyboardComponent);
    fixture = testContext.fixture;
    component = testContext.component;
    component.inputId = 'input-id1';
    component.inputFocus = true;
    addTestingIconsMock(testContext);
    fixture.detectChanges();
    keyboardButton = testContext.getElByCss('button.ml-2');
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render the label name', () => {
    const labelText = 'Testing Label';
    fixture.componentInstance.labelTitle = labelText;
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('label').textContent).toEqual(labelText);
  });

  it('should open keyboard modal', () => {
    spyOn(component, 'displayOnScreenKeyboard').and.callThrough();
    keyboardButton.click();
    fixture.detectChanges();
    expect(component.displayOnScreenKeyboard).toHaveBeenCalled();
  });

  it('destroy component should close valueChangesSubscription', () => {
    fixture.destroy();
    expect(component.valueChangesSubscription.closed).toEqual(true);
  });

  it('should close the keyboard', () => {
    spyOn(component, 'onTabEnterPressed').and.callThrough();
    keyboardButton.click();
    fixture.detectChanges();
    const closeBtn = document.querySelector('button.treo-message-dismiss-button');
    (closeBtn as HTMLButtonElement).click();
    fixture.detectChanges();
    expect(component.onTabEnterPressed).not.toHaveBeenCalled();
  });

  it('should enter the value', () => {
    component.closeOnReturn = true;
    keyboardButton.click();
    fixture.detectChanges();
    const inputKeyboard = document.querySelector<HTMLInputElement>('#input-id1');
    typeInElement('123', inputKeyboard);
    fixture.detectChanges();
    const enterBtn = document.querySelector<HTMLDivElement>('div.hg-button-enter');
    enterBtn.click();
    fixture.detectChanges();
    expect(component.form.value.input).toEqual('123');
  });

  it('should select options the value', () => {
    component.value = 'RC';
    component.optionsLabel = 'name';
    component.options = [
      {
        name: 'RC PL TRIPLE',
        selectionKey: '1',
      },
      {
        name: 'RC PL QUAD',
        selectionKey: '2',
      },
      {
        name: 'RC2D TRIPLE',
        selectionKey: '3',
      },
      {
        name: 'RC TRIPLE',
        selectionKey: '4',
      },
      {
        name: 'RC DOUBLE',
        selectionKey: '5',
      },
    ];
    fixture.detectChanges();
    keyboardButton.click();
    fixture.detectChanges();
    const inputKeyboard = document.querySelector<HTMLInputElement>('#input-id1');
    typeInElement('RC', inputKeyboard);
    fixture.detectChanges();
    spyOn(component.optionSelected, 'emit').and.callThrough();
    const optionBtn1 = document.querySelector<HTMLButtonElement>('div.options-container > button:nth-of-type(1)');
    optionBtn1.click();
    fixture.detectChanges();
    expect(component.optionSelected.emit).toHaveBeenCalledWith(component.options[0]);
  });
});
