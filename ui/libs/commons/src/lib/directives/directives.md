## DIRECTIVES

### Control Error Directive

This directive is used automatically in all reactive forms field through `formControlName` or `formControl` directives, the directive
get the updateOn property from the control and set the validateOn input appropriately and will throw a warning when updateOn and validateOn
are different and validateOn is one of the defaults update strategies, to disable this behavior you can use `rsaNoControlErrors` directive on the field.

When using a `matInput` is important to use this directive along with `errorStateMatcher` property of the Material `matInput` to get a correct synchronization
between the control error directive and material. The property takes an instance of an ErrorStateMatcher object. An ErrorStateMatcher must implement a single
method isErrorState which takes the FormControl for this matInput as well as the parent form and returns a boolean indicating whether errors should be shown.
For more information go to https://material.angular.io/components/input/overview and search for `Changing when error messages are shown`.

##### Selector: `[formControlName]:not([rsaNoControlErrors])], [formControl]:not([rsaNoControlErrors])`

##### Inputs:

1. `customErrors: object` Custom errors object depending on the validators either angular defaults, or your custom validators
   defaults: (required, pattern, requiredTrue, email, min, max, minlength, maxlength).
2. `validateOn: ` Scenario to run validations 'change' | 'enter' | 'touchedState' | 'blur' | 'submit';
   - change: It validates on value change event.
   - blur: It validates on control blur event.
   - submit: It validates on form submit event.
   - enter: It validates on enter event.
   - touchedState: It validates on value changes after the control touched attribute is true or first blur event.

##### Output:

1. `validation: EventEmitter<boolean>` Custom errors object depending on the validators either angular defaults, or your custom validators
   defaults: (required, pattern, requiredTrue, email, min, max, minlength, maxlength).

##### Example:

```html
<div class="flex items-center w-full" [formGroup]="form">
  <label class="mr-4 font-bold">Label</label>
  <mat-form-field class="flex-auto treo-mat-no-subscript">
    <input matInput formControlName="input" [customErrors]="{required: 'Item required'}" />
    <!-- Will place the message here -->
  </mat-form-field>
</div>
```

### Control Error Container Directive

This directive along with `Control Error Directive` you can place the error message as a sibling where this directive is used

##### Selector: `rsaControlErrorContainer`

##### Example:

```html
<div class="flex items-center w-full" [formGroup]="form">
  <label class="mr-4 font-bold">Label</label>
  <mat-form-field class="flex-auto treo-mat-no-subscript" rsaControlErrorContainer>
    <input matInput formControlName="input" [customErrors]="{required: 'Item required'}" />
  </mat-form-field>
  <!-- Will place the message here -->
</div>
```

### No Control Errors

This directive is used to disable the behavior of `Control Error Directive` if not needed in one field `formControlName` or `formControl`.

##### Selector: `rsaNoControlErrors`

##### Example:

```html
<div class="flex items-center w-full" [formGroup]="form">
  <label class="mr-4 font-bold">Label</label>
  <mat-form-field class="flex-auto treo-mat-no-subscript">
    <input matInput formControlName="input" rsaNoControlErrors />
    <!-- Will not show any message on validation because you use rsaNoControlErrors directive -->
  </mat-form-field>
</div>
```

### Auto Focus If

This directive is used to focus and input element if a variable is true.

##### Selector: `rsaAutoFocusIf`

##### Inputs:

1. `rsaAutoFocusIf: boolean` Boolean expression.

##### Example:

```js
@Component({
  template: ` <div class="testContainer" [formGroup]="formGroup">
    <input type="number" placeholder="$150.00" formControlName="formControlName" [(rsaAutoFocusIf)]="focused" />
  </div>`,
})
class AutoFocusWrapperComponent {
  focused = true;
  formGroup = new FormGroup({
    formControlName: new FormControl(122),
  });
}
```

### Mask Regex

This directive is used to create a mask on an input to limit the characters allowed to type

##### Selector: `rsaMaskRegex`

##### Inputs:

1. `allowedCharsRegex: string` Allowed Characters Regex.

##### Example:

```js
@Component({
  template: ` <input type="text" rsaMaskRegex allowedCharsRegex="[0-9]+" [formControl]="formControl" /> `,
})
class MaskRegexDirectiveComponent {
  formControl = new FormControl('100');
}
```

### Barcode

This directive is used to validate barcode and strip unwanted characters from scanners

##### Selector: `rsaBarcode`

##### Example:

```js
@Component({
  template: `<rsa-input-keyboard
    id="scanUnitNumberField"
    [inputWidth]="'w-80'"
    [formControl]="formControl"
    rsaBarcode
    [iconName]="'opacity'"
    [(inputFocus)]="unitFocus"
    [labelClasses]="'w-30'"
    [labelTitle]="'unit-number.label'"
    (tabOrEnterPressed)="onScannedUnit($event)"
    [inputId]="'scanUnitNumberInput'"
  ></rsa-input-keyboard>`,
})
class MaskRegexDirectiveComponent {
  formControl = new FormControl('');
}
```

### MinValueValidator

This directive is used to validate the minimun value for an input without formcontrol - using ngmodel

##### Selector: `rsaMinValueValidator`

##### Example:

```js
@Component({
  template: ` <input type="text" [rsaMinValueValidator]="1" [(ngModel)]="myField" /> `,
})
class MinValueValidatorDirectiveComponent {
  myField: number;
}
```

### PermissionsOnlyDirective

This directive is used to disable an element if the user does not own the specified permissions in the directive

##### Selector: `rsaPermissionsOnly`

##### Example:

```js
@Component({
  template: `
    <button
      id="editOrderBtn"
      class="bg-orange-light"
      [rsaPermissionsOnly]="['ROLE_ORDERS_ALL', 'ROLE_CREATE_ORDER_ALL']"
      mat-stroked-button
      (click)="onEditOrder()"
    >
      {{ 'edit-order.label' | translate }}
    </button>
  `,
})
class PermissionsOnlyDirectiveComponent {
  onEditOrder() {}
}
```
