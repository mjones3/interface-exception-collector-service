import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule, AsyncValidatorFn } from '@angular/forms';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { TemperatureFormComponent } from './temperature-form.component';
import { of } from 'rxjs';

jest.mock('keycloak-js');

describe('TemperatureFormComponent', () => {
  let component: TemperatureFormComponent;
  let fixture: ComponentFixture<TemperatureFormComponent>;
  let mockAsyncValidator: AsyncValidatorFn;

  beforeEach(async () => {
    mockAsyncValidator = jest.fn(() => of(null));
    
    await TestBed.configureTestingModule({
      imports: [
        TemperatureFormComponent,
        ReactiveFormsModule,
        NoopAnimationsModule,
        MatDatepickerModule,
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(TemperatureFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should enable form and set validators when useTemperature is true', () => {
    component.updateValidators(true, mockAsyncValidator);
    const form = component.formGroup();
    
    expect(form.enabled).toBeTruthy();
    expect(form.get('thermometerId')?.hasError('required')).toBeTruthy();
    expect(form.get('temperature')?.disabled).toBeTruthy();
  });

  it('should disable form and clear validators when useTemperature is false', () => {
    component.updateValidators(false, mockAsyncValidator);
    const form = component.formGroup();
    expect(form.disabled).toBeTruthy();
  });

  it('should validate temperature range', () => {
    component.updateValidators(true, mockAsyncValidator);
    const tempControl = component.formGroup().get('temperature');
    
    tempControl?.enable();
    
    tempControl?.setValue(-300);
    tempControl?.markAsTouched();
    tempControl?.updateValueAndValidity();
    expect(tempControl?.hasError('min')).toBe(true);
    
    tempControl?.setValue(100);
    tempControl?.updateValueAndValidity();
    expect(tempControl?.hasError('max')).toBe(true);
    
    tempControl?.setValue(25);
    tempControl?.updateValueAndValidity();
    expect(tempControl?.valid).toBe(true);
  });

  it('should reset form', () => {
    const form = component.formGroup();
    form.patchValue({ thermometerId: 'test123', temperature: 25 });
    
    component.reset();
    
    expect(form.get('thermometerId')?.value).toBeNull();
    expect(form.get('temperature')?.value).toBeNull();
  });

  it('should blur element on triggerElementBlur', () => {
    const mockElement = { blur: jest.fn() } as any;
    const mockEvent = { target: mockElement } as Event;
    
    component.triggerElementBlur(mockEvent);
    
    expect(mockElement.blur).toHaveBeenCalled();
  });
});