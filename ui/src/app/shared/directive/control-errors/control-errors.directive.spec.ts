import { CommonModule } from '@angular/common';
import { Component, ElementRef, OnDestroy, QueryList, ViewChild, ViewChildren } from '@angular/core';
import { ComponentFixture, fakeAsync, TestBed, tick, waitForAsync } from '@angular/core/testing';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { By } from '@angular/platform-browser';
import { Subscription } from 'rxjs';
import { ControlErrorComponent } from '../../components/control-error/control-error.component';
import { ControlErrorsDirective } from './control-errors.directive';

/***** Test Wrapper Component *****/
@Component({
  template: ` <form class="testContainer" [formGroup]="formGroup" rsaFormSubmit>
    <div #containerEl rsaControlErrorContainer>
      <input
        class="e7_input form-control"
        type="number"
        formControlName="formControlName"
        [customErrors]="customErrors"
      />
      <button #buttonElement class="btn" type="submit">Submit</button>
    </div>
  </form>`,
})
class ControlErrorTestWrapperComponent implements OnDestroy {
  readonly minValue = 150;
  @ViewChild('containerEl', { static: false }) containerEl: ElementRef;
  @ViewChild(ControlErrorsDirective, { static: false }) errorsDirective: ControlErrorsDirective;
  @ViewChildren(ControlErrorComponent) errors: QueryList<ControlErrorComponent>;
  @ViewChild('buttonElement', { static: false }) buttonEl: ElementRef;
  customErrors = {
    min: `Value should be less than ${this.minValue}`,
  };
  formGroup: FormGroup = new FormGroup({
    formControlName: new FormControl(100, [Validators.min(this.minValue)]),
  });

  ngOnDestroy(): void {}
}

describe('ControlErrorsDirective', () => {
  let component: ControlErrorTestWrapperComponent;
  let fixture: ComponentFixture<ControlErrorTestWrapperComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        declarations: [ControlErrorTestWrapperComponent],
        imports: [CommonModule, ReactiveFormsModule],
      });
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(ControlErrorTestWrapperComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should show error', done => {
    fakeAsync(() => {
      component.formGroup.patchValue({ formControlName: 120 });
      tick(250);
      const matErrorEl = fixture.debugElement.query(By.css('mat-error.mat-error')).nativeElement as HTMLElement;
      fixture.detectChanges();
      expect(matErrorEl.innerHTML.trim()).toEqual(`Value should be less than ${component.minValue}`);
      expect((component.containerEl.nativeElement as HTMLDivElement).classList.contains('control-error')).toEqual(true);
      done();
    })();
  });

  it('should hide the error', done => {
    fakeAsync(() => {
      component.formGroup.patchValue({ formControlName: 120 });
      tick(250);
      const matErrorEl = fixture.debugElement.query(By.css('mat-error.mat-error')).nativeElement as HTMLElement;
      component.formGroup.patchValue({ formControlName: 180 });
      tick(250);
      fixture.detectChanges();
      expect(matErrorEl.innerHTML).toEqual('');
      expect((component.containerEl.nativeElement as HTMLDivElement).classList.contains('control-error')).toEqual(
        false
      );
      done();
    })();
  });

  it('should show error when submit the form', done => {
    fakeAsync(() => {
      component.formGroup.patchValue({ formControlName: 100 });
      component.buttonEl.nativeElement.click();
      tick(250);
      fixture.detectChanges();
      const matErrorEl = fixture.debugElement.query(By.css('mat-error.mat-error')).nativeElement as HTMLElement;
      expect(matErrorEl.innerHTML.trim()).toEqual(`Value should be less than ${component.minValue}`);
      done();
    })();
  });

  it('should get a subscription', () => {
    const subscription = component.errorsDirective.getSubscription();
    expect(subscription instanceof Subscription).toEqual(true);
  });
});
