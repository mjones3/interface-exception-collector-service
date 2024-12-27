# Menu Component

The Menu component provides a floating panel containing a list of options through a 3 dot icon.

## Properties

### Inputs

`@Input({ required: true }) menus: TableMenu[]` List of options to be displayed as a floating panel.

-   `id: string` Option id.
-   `label: string` Text to be displayed in option.
-   `subMenu?: TableMenu` Sub option item.
-   `icon?: string` Icon to be displayed.
-   `click?: () => void` Option item click function.

## Example of Use

Examples

```
<biopro-menu [menus]="menus"></biopro-menu>
```
