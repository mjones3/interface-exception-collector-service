# On Screen Keyboard Common Component

The On Screen Keyboard displays a virtual keyboard using simple-keyboard library. (https://hodgef.com/simple-keyboard/) 

## Properties
### Inputs

`@Input() keyboardType: KeyboardTypeEnum` Keyboard type to display `NUMERIC/TEXT`. Default value is `TEXT`.

`@Input() closeOnReturn: boolean` Whether to allow close the on-screen-keyboard modal when the user presses the Enter key, or the Submit button when is set to `true` which is the default value or keep it open when is set to `false`. If the input type is `TEXTAREA` the modal remains open when pressed the Enter Key, in this case, Enter Key creates a line break.

`@Input() inputType: InputType` Input type `TEXTAREA/TEXT`. Default value `TEXT`.

`@Input() placeholder: string` Input placeholder.

`@Input() pattern: string` Pattern validation.
  
`@Input() patternMessage: string` Text message for pattern validation.

`@Input() regex: string` Regex validation.

`@Input() options: any` Collection of items passed to the on-screen-keyboard when using the component in a filterable input.

`@Input() optionsLabel: string` Property key in the `options` list (if `options` is a list of objects) to be displayed as the list label.
  
`@Input() maxLength: number` Input MaxLength.

`@Input() value: string` Initial input value.

`@Input() inputTemplate: TemplateRef<any>` Template to use a custom input.

`@Input() textareaRows: number` Amount of rows visible when `inputType = TEXTAREA`.

### Outputs

`@Output() returnPressed: EventEmitter<string>` Event emitted when pressed the Enter key. 

`@Output() optionSelected: EventEmitter<Option>` Event emitted when using the component for a filter input, and the user select an option from the on-screen-keyboard. 
