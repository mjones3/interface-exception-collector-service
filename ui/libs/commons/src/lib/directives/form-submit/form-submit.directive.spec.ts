import {Component, ElementRef, ViewChild} from '@angular/core';
import {ComponentFixture, fakeAsync, TestBed, tick, waitForAsync} from '@angular/core/testing';
import {FormControl, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {RsaCommonsModule} from '@rsa/commons';
import {FormSubmitDirective} from './form-submit.directive';

/***** Test Wrapper Component *****/
@Component({
  template: `
    <form #formEl class="testContainer" [formGroup]="formGroup" rsaFormSubmit (ngSubmit)="submitForm(formGroup.value)">
      <input class="e7_input form-control" type="number" aria-label="Aria" validateOn="submit"
             formControlName="formControlName">
      <button #buttonElement class="btn btn-secondary btn-md" type="submit" [disabled]="!formGroup.valid">Submit
      </button>
    </form>`,
})
class FormSubmitTestWrapperComponent {
  @ViewChild('formEl', {static: false}) formElRef: ElementRef;
  @ViewChild('buttonElement', {static: false}) buttonEl: ElementRef;
  formGroup: FormGroup = new FormGroup({
    formControlName: new FormControl('155')
  });

  submitForm(values) {
    console.log('submit', values);
  }
}

describe('FormSubmitDirective', () => {
  let component: FormSubmitTestWrapperComponent;
  let fixture: ComponentFixture<FormSubmitTestWrapperComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [FormSubmitTestWrapperComponent],
      imports: [ReactiveFormsModule, RsaCommonsModule]
    });
    fixture = TestBed.createComponent(FormSubmitTestWrapperComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('It should focus on element', (done) => {
    fakeAsync(() => {
      component.buttonEl.nativeElement.click();
      tick(1200);
      fixture.detectChanges();
      const classList = (component.formElRef.nativeElement as HTMLFormElement).classList;
      expect(classList.contains('submitted')).toEqual(true);
      done();
    })();
  });
});
