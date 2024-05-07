# Unit Object for Batch Screen Common Component

The Unit Object for Batch Screen component can be used in different processes in the application. When the user scans a unit, if the unit has more than one product, it will populate a modal with the products in the inventory, the user can select the desired product, and the modal will close adding the selection to the Added Units section. If the Unit has only one product in the inventory it will be added automatically to the Added Units area. 

## Properties

### Inputs

`@Input headerTitle: string` Header title for the Added Units section. Default value `'Added Units'`.   

`@Input() showCentrifugeCode: boolean` Centrifuge Code input will be visible when this value is `true`.

`@Input() addedUnits: Product[]` List of products on the Added Units section.

`@Input() productsInventory: any[]` List of the product in inventory of the scanned unit.

`@Input() productLabel: string` Object property in `productsInventory` to display as label. As default `'descriptionKey'`.

`@Input() allUnitsAddedToasterTitle: string` Contains the toaster title for the error when the user scans a unit and all available products are already in the Added Units List.

`@Input() allUnitsAddedToasterMessage: string` Contains the toaster message for the error when the user scans a unit and all available products are already in the Added Units List.

`@Input() header: TemplateRef<any>` Template to displayed in the header area before the Added Units section.

`@Input() footer: TemplateRef<any>` Template to displayed in the bottom area after the Added Units section.

`@Input() useCustomUnitCard: boolean` Use custom unit card to show unit details.

`@Input() addProductDirectly: boolean` If true, when the user selects a product from the modal after scan a unit it will be added to Add Units section automatically if set to false it will trigger an event with the selected product.

### Output

`@Output() unitNumberScanned` Emits an event when a user scans a unit number.

`@Output() centrifugeBarcodeScanned` Emits an event when a user scans a centrifuge barcode.

`@Output() clearUnitNumber` Emits an event when the user Delete all the Added Units.

`@Output() productSelected` Emits an event when the user select a product after scanned a unit and addProductDirectly is false.
