import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { By } from '@angular/platform-browser';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { RsaCommonsModule, RsaValidators } from '@rsa/commons';
import { MaterialModule } from '@rsa/material';
import { createTestContext, typeInElement } from '@rsa/testing';
import { InputType, KeyboardTypeEnum } from '@rsa/touchable';
import 'jest-canvas-mock';
import { addTestingIconsMock } from '../../shared/testing/mocks/data/icons.mock';
import { OnScreenKeyboardComponent } from './on-screen-keyboard.component';

describe('OnScreenKeyboardComponent', () => {
  let component: OnScreenKeyboardComponent;
  let fixture: ComponentFixture<OnScreenKeyboardComponent>;
  let optionBtn: HTMLButtonElement;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [OnScreenKeyboardComponent],
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
    const testContext = createTestContext<OnScreenKeyboardComponent>(OnScreenKeyboardComponent);
    fixture = testContext.fixture;
    component = testContext.component;
    addTestingIconsMock(testContext);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('On change value', () => {
    const value = 'value';
    component.onChange(value);
    expect(component.value).toEqual(value);
  });

  it('On change value with options', () => {
    const value = 'op';
    component.options = [
      { selectionKey: '1', name: 'Option1' },
      { selectionKey: '2', name: 'Option2' },
    ];
    component.onChange(value);
    expect(component.value).toEqual(value);
    expect(component.filteredOptions.length).toEqual(2);
  });

  it('Validate And Emit Value', done => {
    const value = 'value';
    component.onChange(value);
    component.returnPressed.subscribe(data => {
      expect(data).toEqual(value);
      done();
    });
    component.validateAndEmitValue();
  });

  it('should show numeric keyboard', () => {
    component.keyboardType = KeyboardTypeEnum.NUMERIC;
    component.ngOnInit();
    fixture.detectChanges();
    const layout = component.keyboard.options.layout['default'];
    expect(layout[0]).toEqual('1 2 3');
    expect(layout[1]).toEqual('4 5 6');
    expect(layout[2]).toEqual('7 8 9');
  });

  it('should select option clicking option button', done => {
    const value = 'Option1';
    component.optionSelected.subscribe(data => {
      expect(data.name).toEqual(value);
      done();
    });
    component.options = [
      { selectionKey: '1', name: 'Option1' },
      { selectionKey: '2', name: 'Option2' },
    ];
    // Typing 'Op' on the input
    const inputEL = fixture.debugElement.query(By.css('mat-form-field input:nth-of-type(1)')).nativeElement;
    typeInElement('Op', inputEL);
    fixture.detectChanges();
    // Clicking in first option button
    optionBtn = fixture.debugElement.query(By.css('div.options-container button:nth-of-type(1)')).nativeElement;
    optionBtn.click();
    expect(component.value).toEqual(value);
    expect(component.actualPage).toEqual(0);
  });

  it('should handle shift keyboard layout', () => {
    component.handleLayoutChange('{shift}');
    expect(component.keyboard.options.layoutName).toEqual('shift');
  });

  it('should handle alt keyboard layout', () => {
    component.handleLayoutChange('{alt}');
    expect(component.keyboard.options.layoutName).toEqual('alt');
  });

  it('should press enter key text field', () => {
    spyOn(component.returnPressed, 'emit').and.callThrough();
    component.onKeyPress('{enter}');
    expect(component.returnPressed.emit).toHaveBeenCalled();
  });

  it('should press enter key text area', () => {
    spyOn(component, 'syncValue').and.callThrough();
    component.inputType = InputType.TEXTAREA;
    component.ngOnInit();
    fixture.detectChanges();
    component.onKeyPress('{enter}');
    expect(component.syncValue).toHaveBeenCalled();
  });

  it('should sync value onInit', () => {
    component.value = 'Test';
    component.ngOnInit();
    expect(component.keyboard.getInput('default')).toEqual(component.value);
  });

  it('sync value onInit', () => {
    component.value = 'Test';
    component.ngOnInit();
    expect(component.keyboard.getInput('default')).toEqual(component.value);
  });

  it('should not emit either option selected or input value', () => {
    component.value = 'W123456';
    component.validators = [RsaValidators.unitNumber];
    component.customErrors = { invalidUnitNumber: 'Invalid unit number' };
    component.ngOnInit();
    fixture.detectChanges();
    expect(component.inputControl.valid).toEqual(false);
  });

  it('should emit input value', () => {
    spyOn(component.returnPressed, 'emit').and.callThrough();
    component.validators = [RsaValidators.unitNumber];
    component.customErrors = { invalidUnitNumber: 'Invalid unit number' };
    fixture.detectChanges();
    component.onChange('W123456789098');
    component.validateAndEmitValue();
    expect(component.returnPressed.emit).toHaveBeenCalled();
  });
});
