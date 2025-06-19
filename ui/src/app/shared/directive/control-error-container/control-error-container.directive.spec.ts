import { CommonModule } from '@angular/common';
import { Component, ElementRef, ViewChild } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';

/***** Test Wrapper Component *****/
@Component({
  template: `
    <form #formEl class="testContainer" [formGroup]="formGroup">
      <div rsaControlErrorContainer>
        <input class="e7_input form-control" type="number" formControlName="formControlName">
      </div>
    </form>`
})
class ControlErrorContainerTestWrapperComponent {
  @ViewChild('formEl', {static: false}) formElRef: ElementRef;
  formGroup: FormGroup = new FormGroup({
    formControlName: new FormControl('155')
  });
}

describe('ControlErrorDirectiveDirective', () => {
  let component: ControlErrorContainerTestWrapperComponent;
  let fixture: ComponentFixture<ControlErrorContainerTestWrapperComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ControlErrorContainerTestWrapperComponent],
      imports: [CommonModule, ReactiveFormsModule]
    });
    fixture = TestBed.createComponent(ControlErrorContainerTestWrapperComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create an instance', () => {
    expect(component).toBeTruthy();
  });
});
