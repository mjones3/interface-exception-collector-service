# Options Picker Dialog Common Component

Component to be populated in a modal. Receives a list of items and display them using buttons. Once the user pick an option the modal will close passing the selected option to the parent component. 

## Inputs

`@Input() options: any` Contains the collection to be displayed.

`@Input() optionsLabel: string` Contains the object property that will be displayed in the button label.

`@Input() headerTitle: string` Contains the modal header title.   

## Outputs

`@Output() optionChange` Emits the options selected every time an option change occurs.
