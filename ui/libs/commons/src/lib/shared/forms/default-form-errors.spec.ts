import { Component, Inject } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MaterialModule } from '@rsa/material';
import { createTestContext } from '@rsa/testing';
import { FORM_ERRORS } from './default-form-errors';

/***** Test Wrapper Component *****/
@Component({
  template: ``,
})
class DefaultFormErrorsTestWrapperComponent {
  constructor(@Inject(FORM_ERRORS) public errors) {}
}

const defaultFunctions = ['required', 'pattern', 'requiredTrue', 'email', 'min', 'max', 'minlength', 'maxlength'];

describe('DefaultFormErrorsTestWrapperComponent', () => {
  let component: DefaultFormErrorsTestWrapperComponent;
  let fixture: ComponentFixture<DefaultFormErrorsTestWrapperComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [DefaultFormErrorsTestWrapperComponent],
      imports: [ReactiveFormsModule, MaterialModule, NoopAnimationsModule],
    });
    const testContext = createTestContext<DefaultFormErrorsTestWrapperComponent>(DefaultFormErrorsTestWrapperComponent);
    fixture = testContext.fixture;
    component = testContext.component;
    fixture.detectChanges();
  });

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  it('should contain default errors functions', () => {
    defaultFunctions.forEach(fx => {
      expect(component.errors[fx] instanceof Function).toEqual(true);
    });
  });

  it('should show required message', () => {
    expect(component.errors.required('Field')).toEqual('Field is required');
  });

  // const defaultFunctions = ['requiredTrue', 'email', 'min', 'max', 'minlength', 'maxlength'];
  it('should show pattern message', () => {
    expect(component.errors.pattern('Field')).toEqual('Field is not valid');
  });
  it('should show requiredTrue message', () => {
    expect(component.errors.requiredTrue('Field')).toEqual('Field must be true');
  });
  it('should show email message', () => {
    expect(component.errors.email('Field')).toEqual('Field should be a valid email');
  });

  it('should show min message', () => {
    expect(component.errors.min('Field', { min: 10, actual: 5 })).toEqual('Field should be higher than 10 but user entered 5');
  });

  it('should show max message', () => {
    expect(component.errors.max('Field', { max: 5, actual: 10 })).toEqual('Field should be lower than 5 but user entered 10');
  });

  it('should show minlength message', () => {
    expect(
      component.errors.minlength('Field', {
        requiredLength: 5,
        actualLength: 3,
      })
    ).toEqual('Field must be at least 5 characters long but user entered 3');
  });

  it('should show maxlength message', () => {
    expect(
      component.errors.maxlength('Field', {
        requiredLength: 5,
        actualLength: 10,
      })
    ).toEqual('Field cannot be more than 5 characters long but user entered 10');
  });
});
