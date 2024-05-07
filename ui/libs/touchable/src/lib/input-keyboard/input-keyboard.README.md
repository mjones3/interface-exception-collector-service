# Input Keyboard Common Component

The Input Keyboard component displays a label, input, and button that trigger the on-screen keyboard.

## Properties

### Inputs

`@Input() labelTitle: string` Text to be displayed as label.

`@Input() labelClasses: string` CSS classes to be applicable to the label.

`@Input() labelWidth: string` CSS width class to be applicable to the label.

`@Input() inputType: InputType` Input type `TEXTAREA/TEXT`.

`@Input() inputWidth: string` CSS width class to be applicable to the input.

`@Input() inputId: string` Input id.

`@Input() textareaRows: number` Amount of rows visible when `inputType = TEXTAREA`.

`@Input() placeholder: string` Input placeholder.

`@Input() iconName: string` Icon for the input.

`@Input() keyboardType: KeyboardTypeEnum` Keyboard type to be triggered from keyboard button `NUMERIC/TEXT`.

`@Input() closeOnReturn: boolean` Whether to allow close the on-screen-keyboard modal when the user presses the Enter key, or the Submit button when is set to `true` which is the default value or keep it open when is set to `false`. If the input type is `TEXTAREA` the modal remains open when pressed the Enter Key, in this case, Enter Key creates a line break.

`@Input() submitFromKeyboard: boolean` Whether to submit the field directly from on-screen-keyboard modal when the user presses the Enter key, or the Submit button when the value is `true`, which is the default value.

`@Input() regex: string` Regex validation.

`@Input() customErrors: string` Object with the error type and custom validation description.

`@Input() options: Option[]` Collection of items passed to the on-screen-keyboard when using the component in a filterable input.

`@Input() optionsLabel: string` Property in options object to be displayed in the label.

`@Input() maxLength: number` Input MaxLength.

`@Input() inputTemplate: TemplateRef<any>` Template to use a custom input.

`@Input() highlightField: boolean` If true highlight the field in red.

### Outputs

`@Output() tabOrEnterPressed: EventEmitter<string>` Event emitted when pressed the Tab or Enter key from the input.

`@Output() inputChange: EventEmitter<string>` Event emitted when the input value changes.

`@Output() optionSelected: EventEmitter<Option>` Event emitted when using the component for a filter input, and the user select an option from the on-screen-keyboard.

## Example of Use

```
<rsa-input-keyboard [formControlName]="'scanUnitNumber'"
                    [inputWidth]="'w-60'"
                    [iconName]="'opacity'"
                    [inputFocus]="true"
                    [pattern]="commonRegex.unitNumber"
                    [patternMessage]="'Invalid unit number'"
                    [inputId]="'unitNumberInput'"
                    (tabOrEnterPressed)="onFormSubmit()">
</rsa-input-keyboard>
```
