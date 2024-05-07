# Confirmation Dialog Common Component

Modal component to be used to confirm an action. 

## Properties
### Inputs

`@Input() dialogTitle: string` Contains the dialog title. 

`@Input() dialogText: string` Contains the confirmation text. 

`@Input() iconName: string` Contains the name of the icon to be displayed on the top section of the text area. The icon section will not be displayed if no name is passed.

`@Input() acceptBtnTittle: string` Contains the text for the confirm button. As default `'Submit'`.

`@Input() cancelBtnTittle: string` Contains the text for the cancel button. As default `'Cancel'`.

`@Input() commentsLabel: string` Label to be displayed for the comments field.

`@Input() commentsModal: boolean` If `true` the modal will contain a comment field and an `ng-content` that will be displayed on the top section of the comment area. This modal will not display the icon section. Comment field is `required`.   

### Outputs

No Outputs.

## Confirmation Result

Once the user select an action of confirm or cancel, the modal will close passing the action result to the parent component.

If `commentsModal` is `true` the modal cannot be accepted unless the user enters a comment.
