import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RsaValidators } from '@rsa/commons';
import { MaterialModule } from '@rsa/material';
import { createTestContext } from '@rsa/testing';

/***** Test Wrapper Component *****/
@Component({
  template: `
    <!--    Testing with Reactive Driven Form-->
    <form id="form1Id" [formGroup]="formGroup">
      <input formControlName="control">
    </form>`
})
class RSAValidatorTestWrapperComponent {
  formGroup: FormGroup = new FormGroup({
    control: new FormControl('', RsaValidators.unitNumber)
  });
}

describe('RSAValidatorTestWrapperComponent', () => {
  let component: RSAValidatorTestWrapperComponent;
  let fixture: ComponentFixture<RSAValidatorTestWrapperComponent>;
  let reactiveInputEl: HTMLInputElement;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [RSAValidatorTestWrapperComponent],
      imports: [ReactiveFormsModule, MaterialModule, NoopAnimationsModule]
    });
    const testContext = createTestContext<RSAValidatorTestWrapperComponent>(RSAValidatorTestWrapperComponent);
    fixture = testContext.fixture;
    component = testContext.component;
    fixture.detectChanges();
    reactiveInputEl = testContext.getElByCss('input[formControlName=\'control\']');
  });

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  it('should be invalid unit number', () => {
    const value = 'Q1312321312312';
    expect(component.formGroup.value.control).toEqual('');
    reactiveInputEl.value = value;
    reactiveInputEl.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    expect(component.formGroup.get('control').invalid).toEqual(true);
  });

  it('should be valid unit number', () => {
    const value = 'W131232131231';
    expect(component.formGroup.value.control).toEqual('');
    reactiveInputEl.value = value;
    reactiveInputEl.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    expect(component.formGroup.get('control').valid).toEqual(true);
  });
});
