import { AfterViewInit, Component, ElementRef, OnDestroy, ViewChild } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { fromEvent, merge, Observable, Subscription } from 'rxjs';
import { AutoUnsubscribe } from './auto-unsubscribe.decorator';

const destroyComponent = (component: any, fixture: ComponentFixture<any>) => {
  jest.spyOn(component, 'ngOnDestroy')
  fixture.destroy();
};

// Test Wrapper Component
@Component({
  template: `
    <form #formEl class="testContainer" [formGroup]="formGroup" (ngSubmit)="submitForm(formGroup)">
      <input class="form-control" type="number" formControlName="formControlName">
      <button #buttonElement class="btn" type="submit">Submit</button>
    </form>`
})
@AutoUnsubscribe()
class AutoUnsubscribeWrapperComponent implements AfterViewInit, OnDestroy {
  @ViewChild('formEl', { static: false }) formElRef: ElementRef;
  @ViewChild('buttonElement', { static: false }) buttonEl: ElementRef;
  formGroup: FormGroup = new FormGroup({
    formControlName: new FormControl('155')
  });
  observable1: Observable<any>;
  observable2: Observable<any>;
  subscription1: Subscription;
  subscription2: Subscription;
  subscription3: Subscription;

  submitForm(_) {
  }

  // Just declared for AutoUnsubscribe decorator
  ngOnDestroy(): void {
  }

  ngAfterViewInit(): void {
    this.observable1 = this.formGroup.valueChanges;
    this.subscription1 = this.observable1.subscribe();
    this.observable2 = fromEvent(this.formElRef.nativeElement, 'submit');
    this.subscription2 = this.observable2.subscribe();
    this.subscription3 = merge(this.observable1, this.observable2).subscribe();
  }
}

describe('AutoUnsubscribeDecorator', () => {
  let component: AutoUnsubscribeWrapperComponent;
  let fixture: ComponentFixture<AutoUnsubscribeWrapperComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [AutoUnsubscribeWrapperComponent],
      imports: [ReactiveFormsModule]
    });
    fixture = TestBed.createComponent(AutoUnsubscribeWrapperComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('It should close the all subscriptions', () => {
    destroyComponent(component, fixture);
    expect(component.ngOnDestroy).toHaveBeenCalled();
    expect(component.subscription1.closed).toEqual(true);
    expect(component.subscription2.closed).toEqual(true);
    expect(component.subscription3.closed).toEqual(true);
  });
});

// --------------------------------------------------------------------------------------------
// Test Wrapper Component for AutoUnsubscribeParamsWrapperComponent
@Component({
  template: `
    <form #formEl class="testContainer" [formGroup]="formGroup" (ngSubmit)="submitForm(formGroup)">
      <input class="form-control" type="number" formControlName="formControlName1">
      <input class="form-control" type="number" formControlName="formControlName2">
      <button #buttonElement class="btn" type="submit">Submit</button>
    </form>`
})
@AutoUnsubscribe({
  blackList: ['subscription4', 'subscription5']
})
class AutoUnsubscribeParamsWrapperComponent implements AfterViewInit, OnDestroy {
  formGroup: FormGroup = new FormGroup({
    formControlName1: new FormControl('155'),
    formControlName2: new FormControl('155')
  });
  subscription4: Subscription;
  subscription5: Subscription;
  subscription6: Subscription;

  ngAfterViewInit(): void {
    this.subscription4 = this.formGroup.get('formControlName1').valueChanges.subscribe();
    this.subscription5 = this.formGroup.get('formControlName2').valueChanges.subscribe();
    this.subscription6 = this.formGroup.valueChanges.subscribe();
  }

  submitForm(_) {
  }

  // Just declared for AutoUnsubscribe decorator
  ngOnDestroy(): void {
  }
}

describe('AutoUnsubscribeDecoratorWithParams', () => {
  let component: AutoUnsubscribeParamsWrapperComponent;
  let fixture: ComponentFixture<AutoUnsubscribeParamsWrapperComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [AutoUnsubscribeParamsWrapperComponent],
      imports: [ReactiveFormsModule]
    });
    fixture = TestBed.createComponent(AutoUnsubscribeParamsWrapperComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('It should close all subscriptions except subscription4 and subscription5', () => {
    destroyComponent(component, fixture);
    expect(component.subscription4.closed).toEqual(false);
    expect(component.subscription5.closed).toEqual(false);
    expect(component.subscription6.closed).toEqual(true);
  });
});

// --------------------------------------------------------------------------------------------
// Test Wrapper Component for AutoUnsubscribeParams1WrapperComponent
@Component({
  template: `
    <form #formEl class="testContainer" [formGroup]="formGroup" (ngSubmit)="submitForm(formGroup)">
      <input class="form-control" type="number" formControlName="formControlName3">
      <input class="form-control" type="number" formControlName="formControlName4">
      <button #buttonElement class="btn" type="submit">Submit</button>
    </form>`
})
@AutoUnsubscribe({
  blackList: ['subscription7'],
  arrayName: 'subscriptionsArray'
})
class AutoUnsubscribeParams1WrapperComponent implements AfterViewInit, OnDestroy {
  formGroup: FormGroup = new FormGroup({
    formControlName3: new FormControl('155'),
    formControlName4: new FormControl('155')
  });
  subscription7: Subscription;
  subscriptionsArray: Subscription[] = [];


  ngAfterViewInit(): void {
    this.subscription7 = this.formGroup.get('formControlName3').valueChanges.subscribe();
    this.subscriptionsArray.push(this.formGroup.get('formControlName4').valueChanges.subscribe());
    this.subscriptionsArray.push(this.formGroup.valueChanges.subscribe());
  }

  submitForm(_) {
  }

  // Just declared for AutoUnsubscribe decorator
  ngOnDestroy(): void {
  }
}

describe('AutoUnsubscribeDecoratorWithOtherParams', () => {
  let component: AutoUnsubscribeParams1WrapperComponent;
  let fixture: ComponentFixture<AutoUnsubscribeParams1WrapperComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [AutoUnsubscribeParams1WrapperComponent],
      imports: [ReactiveFormsModule]
    });
    fixture = TestBed.createComponent(AutoUnsubscribeParams1WrapperComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('It should close subscriptionsArray but not subscription7', () => {
    destroyComponent(component, fixture);
    expect(component.subscription7.closed).toEqual(false);
    component.subscriptionsArray.forEach((subscription) => {
      expect(subscription.closed).toEqual(true);
    });
  });
});

// --------------------------------------------------------------------------------------------
// Test Wrapper Component for AutoUnsubscribeParams1WrapperComponent
@Component({
  template: `
    <form #formEl class="testContainer" [formGroup]="formGroup" (ngSubmit)="submitForm(formGroup)">
      <input class="form-control" type="number" formControlName="formControlName5">
      <input class="form-control" type="number" formControlName="formControlName6">
      <button #buttonElement class="btn" type="submit">Submit</button>
    </form>`
})
@AutoUnsubscribe({
  blackList: ['subscription10'],
  arrayName: 'subscriptionsArray',
  eventFn: 'onNewDestroy'
})
class AutoUnsubscribeParamsEventWrapperComponent implements AfterViewInit {
  formGroup: FormGroup = new FormGroup({
    formControlName5: new FormControl('155'),
    formControlName6: new FormControl('155')
  });
  subscription10: Subscription;
  subscriptionsArray: Subscription[] = [];


  ngAfterViewInit(): void {
    this.subscription10 = this.formGroup.get('formControlName5').valueChanges.subscribe();
    this.subscriptionsArray.push(this.formGroup.get('formControlName6').valueChanges.subscribe());
    this.subscriptionsArray.push(this.formGroup.valueChanges.subscribe());
  }

  submitForm(_) {
  }

  onNewDestroy(): void {
  }
}

describe('AutoUnsubscribeDecoratorWithParamsEvent', () => {
  let component: AutoUnsubscribeParamsEventWrapperComponent;
  let fixture: ComponentFixture<AutoUnsubscribeParamsEventWrapperComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [AutoUnsubscribeParamsEventWrapperComponent],
      imports: [ReactiveFormsModule]
    });
    fixture = TestBed.createComponent(AutoUnsubscribeParamsEventWrapperComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('It should close subscriptionsArray but not subscription7 on new onNewDestroy method', () => {
    component.onNewDestroy();
    expect(component.subscription10.closed).toEqual(false);
    component.subscriptionsArray.forEach((subscription) => {
      expect(subscription.closed).toEqual(true);
    });
  });
});
