import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { InputComponent } from './input.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { FormBuilder, FormControl, FormsModule, NgControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { Component, ViewChild } from '@angular/core';
import { InputType } from 'app/shared/types/input-type.enum';
import { Autocomplete } from 'app/shared/types/autocomplete.enum';

@Component({
  template: `
    <biopro-input
      [inputId]="'test-input'"
      [formControl]="control"
      [label]="'Test Label'"
      [inputType]="inputType"
      [upperCase]="upperCase"
      [required]="required"
      [readOnly]="readOnly"
      [inputFocus]="inputFocus"
      (inputChange)="onInputChange($event)"
      (tabOrEnterPressed)="onTabOrEnterPressed($event)"
      (keyUp)="onKeyUp($event)"
      (inputBlur)="onInputBlur($event)">
    </biopro-input>
  `
})
class TestHostComponent {
  @ViewChild(InputComponent) inputComponent: InputComponent;
  control = new FormControl('');
  inputType = InputType.TEXT;
  upperCase = false;
  required = false;
  readOnly = false;
  inputFocus = false;

  onInputChange(value: string) {}
  onTabOrEnterPressed(value: string) {}
  onKeyUp(value: string) {}
  onInputBlur(value: string) {}
}

describe('InputComponent', () => {
  let component: InputComponent;
  let hostComponent: TestHostComponent;
  let fixture: ComponentFixture<TestHostComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [TestHostComponent],
      imports: [
        InputComponent,
        BrowserAnimationsModule,
        FormsModule,
        ReactiveFormsModule
      ],
      providers: [FormBuilder]
    }).compileComponents();

    fixture = TestBed.createComponent(TestHostComponent);
    hostComponent = fixture.componentInstance;
    fixture.detectChanges();
    component = hostComponent.inputComponent;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Input Properties', () => {
    it('should initialize with default values', () => {
      expect(component.inputType).toBe(InputType.TEXT);
      expect(component.upperCase).toBeFalsy();
      expect(component.textareaRows).toBe(5);
      expect(component.placeholder).toBe('');
      expect(component.regex).toBe('');
      expect(component.allowAutocomplete).toBe(Autocomplete.OFF);
      expect(component.required).toBeFalsy();
      expect(component.readOnly).toBeFalsy();
    });

    it('should require inputId', () => {
      expect(component.inputId).toBeTruthy();
    });
  });

  describe('Form Control Integration', () => {
    it('should update form control value when input changes', () => {
      const testValue = 'test value';
      hostComponent.control.setValue(testValue);
      fixture.detectChanges();
      expect(component.form.get('input').value).toBe(testValue);
    });

    it('should emit changes when form value changes', () => {
      const testValue = 'new value';
      const spy = jest.spyOn(component.inputChange, 'emit');
      component.form.get('input').setValue(testValue);
      fixture.detectChanges();
      expect(spy).toHaveBeenCalledWith(testValue);
    });
  });

  describe('Event Handling', () => {
    it('should emit on tab/enter pressed', () => {
      const spy = jest.spyOn(component.tabOrEnterPressed, 'emit');
      const testValue = 'test value';
      const mockEvent = { preventDefault: jest.fn() } as unknown as Event;
      component.form.get('input').setValue(testValue);
      component.onTabEnterPressed(mockEvent);
      expect(spy).toHaveBeenCalledWith(testValue);
      expect(mockEvent.preventDefault).toHaveBeenCalled();
    });

    it('should emit on blur', () => {
      const spy = jest.spyOn(component.inputBlur, 'emit');
      const testValue = 'test value';
      component.form.get('input').setValue(testValue);
      component.fireOnInputBlur();
      expect(spy).toHaveBeenCalledWith(testValue);
    });

    it('should emit key up events', () => {
      const spy = jest.spyOn(component.keyUp, 'emit');
      const event = new KeyboardEvent('keyup', { key: 'a' });
      component.handleKeyUp(event);
      expect(spy).toHaveBeenCalledWith('a');
    });
  });

  describe('Focus Handling', () => {
    it('should update focus state with delay', fakeAsync(() => {
      component.inputFocus = true;
      tick(200);
      expect(component.inputFocus).toBeTruthy();
      component.inputFocus = false;
      tick(200);
      expect(component.inputFocus).toBeFalsy();
    }));
  });

  describe('Error Handling', () => {
    it('should display custom error messages', () => {
      const customErrors = { required: 'Field is required' };
      component.customErrors = customErrors;
      component.form.get('input').setErrors({ required: true });
      expect(component.errorMessage()).toBe('Field is required');
    });
  });

  describe('Value Accessor Implementation', () => {
    it('should implement writeValue correctly', () => {
      const testValue = 'test value';
      component.writeValue(testValue);
      expect(component.form.get('input').value).toBe(testValue);
      component.writeValue(null);
      expect(component.form.get('input').value).toBe('');
    });

    it('should handle blur events', () => {
      const spy = jest.spyOn(component, 'onTouched');
      component.onBlur();
      expect(spy).toHaveBeenCalled();
    });
  });
});
