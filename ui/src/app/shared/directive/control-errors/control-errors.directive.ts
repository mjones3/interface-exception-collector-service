import {
  ChangeDetectorRef,
  ComponentFactoryResolver,
  ComponentRef,
  Directive,
  DoCheck,
  ElementRef,
  EventEmitter,
  Host,
  HostListener,
  Inject,
  Input,
  OnDestroy,
  OnInit,
  Optional,
  Output,
  Renderer2,
  ViewContainerRef,
} from '@angular/core';
import { NgControl } from '@angular/forms';
import { startCase } from 'lodash';
import { isFunction } from 'lodash-es';
import { EMPTY, fromEvent, iif, merge, Observable, Subject, Subscription } from 'rxjs';
import { debounceTime, delayWhen, distinctUntilChanged, filter, skip, tap } from 'rxjs/operators';
import { ControlErrorComponent } from '../../../shared/components/control-error/control-error.component';
import { FORM_ERRORS } from '../../../shared/forms/default-form-errors';
import { ControlErrorContainerDirective } from '../control-error-container/control-error-container.directive';
import { FormSubmitDirective } from '../form-submit/form-submit.directive';

export type ControlErrorValidateOnType = 'change' | 'enter' | 'touchedState' | 'blur' | 'submit';

@Directive({
  // tslint:disable-next-line:directive-selector
  standalone: true,
  selector: '[formControlName]:not([rsaNoControlErrors])], [formControl]:not([rsaNoControlErrors])',
  exportAs: 'controlError',
})
export class ControlErrorsDirective implements OnInit, OnDestroy, DoCheck {
  /**
   * Any object the contains error key and values
   */
  @Input() customErrors: any = {};

  /**
   * When the control validation is triggered for the defaults update on strategies is not necessary to set this input
   * only for `touchedState` ond `enter` is required
   */
  @Input() validateOn: ControlErrorValidateOnType;

  /**
   * Emit after every control validation
   */
  @Output() validation: EventEmitter<any> = new EventEmitter<any>();

  componentRef: ComponentRef<ControlErrorComponent>;
  container: ViewContainerRef;
  submit$: Observable<Event>;
  touchedState: Subject<boolean> = new Subject<boolean>();
  touchedState$: Observable<boolean> = this.touchedState.asObservable();
  private subscription: Subscription;
  private asyncValidatorsSub: Subscription;
  private readonly errorClass = 'control-error';
  private readonly updateOnStrategies = ['change', 'blur', 'submit'];

  constructor(
    private el: ElementRef,
    private vcr: ViewContainerRef,
    private resolver: ComponentFactoryResolver,
    private controlDir: NgControl,
    private renderer2: Renderer2,
    @Inject(FORM_ERRORS) private errors,
    @Optional() @Host() private form: FormSubmitDirective,
    @Optional() controlErrorContainer: ControlErrorContainerDirective
  ) {
    this.container = controlErrorContainer ? controlErrorContainer.vcr : vcr;
    this.submit$ = this.form ? this.form.submit$ : EMPTY;
  }

  ngOnInit() {
    this.checkAndSetValidateOn();

    switch (this.validateOn) {
      /**
       * Value change validates when value change event is triggered
       */
      case 'change':
        this.setupValueChangeValidation();
        break;
      /**
       * Blur validates when control blur event is triggered
       */
      case 'blur':
        this.setupBlurValidation();
        break;
      /**
       * Submit validates when form submit event is triggered
       */
      case 'submit':
        this.setupSubmitValidation();
        break;
      /**
       * Enter validates when enter event is triggered
       */
      case 'enter':
        this.setupEnterValidation();
        break;
      /**
       * Touched state validates when a value changes after the control is touched
       */
      case 'touchedState':
        this.setupTouchedStateValidation();
    }
  }

  ngDoCheck(): void {
    if (this.validateOn && this.validateOn === 'touchedState') {
      this.touchedState.next(this.control?.touched);
    }
  }

  get control() {
    return this.controlDir.control;
  }

  /**
   * Set the validateOn input if not provided and check for differences with control updateOn property
   */
  private checkAndSetValidateOn() {
    if (!this.validateOn) {
      this.validateOn = this.control.updateOn;
    } else {
      if (this.validateOn !== this.control.updateOn && this.updateOnStrategies.includes(this.validateOn)) {
        console.warn(
          `Using a validateOn (${this.validateOn}) different to the control updateOn property (${this.control.updateOn}) could lead to unexpected behavior.`
        );
      }
    }
  }

  /**
   * Validation on blur (Element must have or implement blur event to use this type of validation)
   */
  private setupBlurValidation(): void {
    const source$ = merge(
      fromEvent(this.el.nativeElement, 'blur').pipe(
        debounceTime(55),
        tap(() => this.markResetControlForValidation())
      ),
      // Emit when the control is reset
      this.getValueChangeOnReset()
    );
    this.validateControlFromObservable(source$);
  }

  private markResetControlForValidation() {
    if (this.control.value === null && this.control.pristine === true) {
      this.control.setValue('');
    }
  }

  /**
   * Validation on enter
   */
  private setupEnterValidation(): void {
    this.validateControlFromObservable(
      fromEvent(this.el.nativeElement, 'keyup').pipe(
        filter((e: KeyboardEvent) => e.key === 'Enter'),
        distinctUntilChanged()
      )
    );
  }

  /**
   * Emit when the status change is not PENDING
   */
  private getStatusChangeSignal(): Observable<any> {
    return this.control.statusChanges.pipe(filter(value => value !== 'PENDING'));
  }

  private getValueChangeOnReset(): Observable<any> {
    return this.control.valueChanges.pipe(filter(() => this.control.value === null));
  }

  private setupValueChangeValidation(): void {
    this.validateControlFromObservable(
      this.control.valueChanges.pipe(
        debounceTime(55),
        tap(value => {
          // Dont mark the control as touched if the control is reset
          if (value !== null) {
            this.control.markAsTouched();
          }
        })
      )
    );
  }

  private setupSubmitValidation(): void {
    const source$ = merge(
      this.submit$.pipe(
        tap(() => {
          /**
           * Every time form is submitted mark all as touched and if the control is in reset state (null) set the
           * control to empty string to be validated
           */
          this.control.markAsTouched();
          this.markResetControlForValidation();
          this.control.updateValueAndValidity();
        })
      ),
      // Emit when the control is reset
      this.getValueChangeOnReset()
    );
    this.validateControlFromObservable(source$);
  }

  private setupTouchedStateValidation(): void {
    const source$ = merge(
      // Value change when the control is touched
      this.control.valueChanges.pipe(
        filter(() => this.control.touched),
        debounceTime(55)
      ),
      // On every change detection when field validateOn is touchedState, use distinctUntilChanged to not emit same state
      // multiple times
      this.touchedState$.pipe(distinctUntilChanged(), skip(1), debounceTime(55)),
      // Emit when the control is reset
      this.getValueChangeOnReset()
    );
    this.validateControlFromObservable(source$);
  }

  private validateControlFromObservable(source$: Observable<any>): void {
    if (this.control.asyncValidator && isFunction(this.control.asyncValidator)) {
      this.subscription = iif(
        () => this.controlHasRequiredError(),
        source$,
        // Wait for the async validator complete to emit
        source$.pipe(
          /**
           * 'delayWhen' is not deprecated seems to be an issue with the docs
           * https://rxjs.dev/api/operators/delayWhen
           */
          delayWhen(() => this.getStatusChangeSignal())
        )
      ).subscribe(() => {
        this.validateControl();
      });
    } else {
      this.subscription = source$.subscribe(() => {
        this.validateControl();
      });
    }
  }

  controlHasRequiredError() {
    return this.control.invalid && !!this.control.errors.required;
  }

  validateControl(): void {
    // debugger
    const controlErrors = this.control.errors;
    if (controlErrors && !this.isResettingControl()) {
      const firstKey = Object.keys(controlErrors)[0];
      const getError = this.errors[firstKey];
      if (this.customErrors[firstKey] || getError) {
        const text =
          this.customErrors[firstKey] || getError(startCase(this.controlDir.name as string), controlErrors[firstKey]);
        this.setError(text);
      }
    } else if (this.componentRef) {
      this.setError(null);
    }
    this.validation.emit();
  }

  private isResettingControl(): boolean {
    return (
      (this.control.value === null && this.control.pristine === true) ||
      (this.control.value === '' && !this.control.touched)
    );
  }

  getContainerNativeElement(): HTMLElement {
    return this.container.element.nativeElement;
  }

  setError(text: string) {
    if (!this.componentRef) {
      const factory = this.resolver.resolveComponentFactory(ControlErrorComponent);
      this.componentRef = this.container.createComponent(factory);
    }
    this.componentRef.instance.text = text;
    this.componentRef.instance.hide = !text;
    const containerNativeEl = this.getContainerNativeElement();
    if (text) {
      this.renderer2.addClass(containerNativeEl, this.errorClass);
    } else {
      this.renderer2.removeClass(containerNativeEl, this.errorClass);
    }
    // Detect changes when the host component got OnPush change detection
    this.componentRef.injector.get(ChangeDetectorRef).detectChanges();
  }

  getSubscription() {
    return this.subscription;
  }

  ngOnDestroy(): void {
    if (this.componentRef) {
      this.componentRef.destroy();
    }
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
    if (this.asyncValidatorsSub) {
      this.asyncValidatorsSub.unsubscribe();
    }
  }

  /**
   * Clear the error label when user inputs data.
   */
  @HostListener('input')
  input(): void {
    this.hideErrorMessage();
  }

  hideErrorMessage(): void {
    if (this.componentRef && this.componentRef.instance) {
      this.componentRef.instance.hide = true;
    }
  }
}
