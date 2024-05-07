# Serile Connection Common Component

Sterile Connection common component will be used across app for the sterile connection process. 

## Inputs

`@Input() definedProcess: string` Process Selection for Select Sterile Connection Process.
 
`@Input() sterileConnectionCompleted: boolean` Set to `true` after first step (Sterile Connection) completed.

`@Input() weldInspectionCompleted: boolean` Set to `true` after second step (Weld Inspection) completed.

`@Input() sterileConnectionProcess: Option[]` Collection to display in Sterile Connection Process select.

`@Input() scdSerialNumber: Option[]` Collection to display in SCD Serial Number select.

`@Input() scdWaferLotNumber: Option[]` Collection to display in SCD Wafer Lot Number select.

`@Input() transferContainerLotNumber: Option[]` Collection to display in Transfer Container Lot Number select.

`@Input() satisfactoryFields: DynamicConfigFields[]` Dynamic fields to be displayed for the Weld Inspection step when the user select Satisfactory inspection. 

##Outputs

`@Output() step1FormValidity` Emits an event when Sterile Connection Form is Valid.

`@Output() step2FormValidity` Emits an event when Weld Inspection Form is Valid.



