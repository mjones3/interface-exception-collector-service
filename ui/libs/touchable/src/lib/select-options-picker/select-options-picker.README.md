# Select Option Picker Common Component

The Table component provides a data-table using Angular Material table that can be used to display rows of data. 

## Properties
### Inputs

`@Input() options: any` Contains the collection to be displayed.

`@Input() optionsLabel: string` Contains the object property that will be displayed in the button label.

`@Input() dialogTitle: string` Modal text header title.  

`@Input() placeholder: string` Placeholder text for the select box.

`@Input() selectId: string` ID for the select box.

`@Input() selectClasses: string` Css classes for the select box.

`@Input() labelTitle: string` Text for the select box label.

`@Input() labelClasses: string` Css classes for the select box label.

`@Input() iconName: string` Icon to use in the input box if the select displays the modal filter.

`@Input() target: any` Template Modal to be populated when clicking on the select if different for Modal Picker or Filter. 

`@Input() dialogConfig: object` Modal configurations.

## Using component as form control

To use the component as part of a form you should past the `formControlName` in your template. Then when you access to the control value you will get the selected option.

Example:  
```
<rsa-select-options-picker [labelTitle]="'Label Title'" 
                           [options]="selectOptionsData" 
                           [optionsLabel]="'name'"
                           [dialogTitle]="dialogTitle" 
                           [placeholder]="'Select Donor Intention'"
                           [selectId]="'donorIntentionSelect'"
                           formControlName="donorIntention">
</rsa-select-options-picker>
``` 
