# Options Picker Common Component

Component receives a list of items and display them using buttons. The user will be able to pick option(s) (single or multiple selection) based on the configuration and the component will emit the option(s) selected. 

## Properties
### Inputs

`@Input() options: any` Contains the collection to be displayed.

`@Input() optionsLabel: string` Contains the object property that will be displayed in the button label.

`@Input() optionsSelected: any` Contains the collection that has been previously selected and will appear disabled.

`@Input() optionsId: string` Contains the id of the buttons wrapper. Default value `'optionButtons'`.

`@Input() headerTitle: string` Contains the header title. If embedded input is true the header will not be shown.  

`@Input() orientation: Orientation` Contains the orientation to be used for the buttons list `Orientation.COLUMN` or `Orientation.ROW`. Default value `Orientation.COLUMN`.

`@Input() maxRows: number` Contains the max number of rows to be displayed when the orientation is `COLUMN`. Default value is `4`. If the number of columns generated exceed `maxColumnsAllowed` this `maxRows` will not be considered.

`@Input() transparentHeader: boolean` If true the header background will be transparent, otherwise it will be white.  

`@Input() embedded: boolean` If true the header and background card will not be shown.  

`@Input() multiple: boolean` If true allows multiple selection. Default value `false`.   

### Outputs

`@Output() optionChange` Emits the options selected every time an option change occurs.

## Using component as form control

To use the Option Picker component as part of a form you should past the `formControlName` in your template. Then when you access to the control value you will get the selected option(s).

Example:  
```
<rsa-options-picker [options]="optionsList" 
                    [optionsLabel]="optionLabel" 
                    formControlName="controlName">
</rsa-options-picker>
```  
