# Filterable Modal Common Component

Filter Modal Component display a list with an input to filter it and when the user selects an item it returns the selection to the parent component. 

## Properties  

### Inputs

`dialogTitle: string` Title to be displayed in the modal.

`inputPlaceholder: string` Placeholder for the filter input.

`iconName: string` Icon name to be showed in the filter input.

`options: any` List of items.

`optionsLabel: string` Property key in the `options` list (if `options` is a list of objects) to be displayed as the list label.

`filterableContainerClasses: string` classes to be applied to the list container.

`pageable: Pageable` Configuration for the pages.

`paginator: Paginator` Current pagination state.

`closable: boolean` If true it will close the modal when user selects an option. 

### Outputs
  
`pageableChange: EventEmitter<Pageable>` Trigger an event when the page changes.
 
## Example of Use

```
const data = {
   options: [{name: 'test', age: 20}, ...], 
   optionsLabel: name,
   inputPlaceholder: 'Enter Name', 
   dialogTitle: 'Select Person', 
   iconName: 'search'
}

const confirmationDialog = this.matDialog.open(FilterableDropDownComponent, data);
confirmationDialog.afterClosed().subscribe(selectedOption => {
            // Process selectedOption
});
```  
