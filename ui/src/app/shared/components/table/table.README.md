# Table Component

The Table component provides a data-table using Angular Material table that can be used to display rows of data.

## Properties

### Inputs

`dataSource: input.required<T[]>()` Contains an array of the data to be displayed in the table.

`@Input() configuration: TableConfiguration` Contains an object with the configurations needed for display the table.

-   `title?: string` Text to be displayed at the top of the table.
-   `menus?: TableMenu[]` List of options to be displayed as a floating panel.
-   `showPagination?: boolean` If set to `true` will display Pagination.
-   `pageSize?: number` Number of items to display on a page.
-   `showExpandAll?: boolean` If set to `true` will display Column Expand All.
-   `expandableKey?: string` Contains the object key in the data source that tells if the specific row has expandable content or not. Used when `expandableRows` is `true`.
-   `selectable?: boolean` If set to `true` will display Column Select (Checkbox).
-   `columns: TableColumn[]` Contains an array of columns to be displayed.
    -   `id: string` Object property in the data source.
    -   `header?: string` Title of the column.
    -   `sort?: boolean` If set to `true` that column will be sortable.
    -   `headerTempRef?: TemplateRef<Element>` Contains the template to be displayed in the column header.
    -   `columnTempRef?: TemplateRef<Element>` Contains the template to be displayed in the column value.
-   `showSorting?: boolean` If set to `true` will Column Sorting.

`@Input({ required: true, transform: booleanAttribute }) serverPagination: boolean` Set to `true` if pagination will happen at server side. Default value `false`.

`@Input({ required: true, transform: numberAttribute }) totalElements: number` Contains the total amount of elements to be displayed in the table when `serverPagination` is `true`.

`@Input() expandTemplateRef: TemplateRef<any>` Contains the template to be displayed in the expandable row section when `expandableRows` is `true`.

`@Input() footerTemplateRef: TemplateRef<any>` Contains the template to be displayed in the footer section.

### Outputs

`@Output() paginate` Emits the pagination information when the user navigates between pages.

`@Output() sort` Emits the sort information when the user changes the column sort.

`@Output() expandingOneOrMoreRows` Emits the expanded rows when the user expands one or more rows to execute lazy loading for the expandable content.

## Example of Use

Examples

```
<biopro-table
    [dataSource]="dataSource"
    [configuration]="tableConfig"
    [totalElements]="totalElements"
    [expandTemplateRef]="expandTemplateRef"
    [serverPagination]="false"
></biopro-table>
<ng-template #expandTemplateRef let-element="element">
    <div class="p-4">
        <p><strong>Comment: </strong>{{ element.comment }}</p>
    </div>
</ng-template>

<biopro-table
    [dataSource]="dataSource"
    [configuration]="tableConfig"
    [totalElements]="totalElements"
    [serverPagination]="true"
    (paginate)="onPaginate($event)"
    (sort)="onSort($event)"
    (expandingOneOrMoreRows)="onExpandingOneOrMoreRows($event)"
></biopro-table>
```
