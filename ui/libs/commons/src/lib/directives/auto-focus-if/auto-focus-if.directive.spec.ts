import {Component, ElementRef, ViewChild} from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import {FormControl, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {AutoFocusIfDirective} from './auto-focus-if.directive';

/***** Test Wrapper Component *****/
@Component({
  template: `
    <div class="testContainer" [formGroup]="formGroup">
      <input #inputEl class="e7_input form-control" type="number" aria-label="Aria"
             placeholder="$150.00" formControlName="formControlName" [(rsaAutoFocusIf)]="focused">
    </div>`,
})
class AutoFocusTestWrapperComponent {
  focused = true;
  @ViewChild('inputEl', {static: false}) inputEl: ElementRef;
  formGroup: FormGroup = new FormGroup({
    formControlName: new FormControl(122)
  });
}

describe('AutoFocusIfDirective', () => {
  let component: AutoFocusTestWrapperComponent;
  let fixture: ComponentFixture<AutoFocusTestWrapperComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [AutoFocusTestWrapperComponent, AutoFocusIfDirective],
      imports: [ReactiveFormsModule]
    });
    fixture = TestBed.createComponent(AutoFocusTestWrapperComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('It should focus on element', () => {
    const classList: DOMTokenList = component.inputEl.nativeElement.classList;
    component.focused = false;
    fixture.detectChanges();
    // expect(classList.contains('ng-touched')).toEqual(true);
  });

});
