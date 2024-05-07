# Table Common Component

The Table component provides a data-table using Angular Material table that can be used to display rows of data. 

## Properties
### Inputs

`@Input() columns: ColumnConfig[]` Contains an array of column configuration to be displayed, where `columnId` is the object property in the data source and `columnHeader` is the header title of the column.

`@Input() dataSource: any[]` Contains an array of the data to be displayed in the table.

`@Input() serverPagination: boolean` Set to `true` if pagination will happen at server side. Default value `false`.

`@Input() totalElements: number` Contains the total amount of elements to be displayed in the table when `serverPagination` is `true`.

`@Input() tableConfiguration: TableConfiguration` Contains an object with the configurations needed for display the table.

  * `showDeleteBtn?: boolean` If set to `true` will display Delete Btn. 
  * `showViewBtn?: boolean` If set to `true` will display View Btn. 
  * `pageSize?: number[]` Array of number of rows that user can select to view per page using the pagination.
  * `showPagination?: boolean` If set to `true` will display Pagination.
  * `showSorting?: boolean` If set to `true` will display Column Sorting.
  * `expandableRows?: boolean` If set to `true` will display expandable row when content passed as `ng-template`.
  * `expandableKey?: string` Contains the object key in the data source that tells if the specific row has expandable content or not. Used when `expandableRows` is `true`. If the evaluation of this key in the data source is `false` the expandable btn will not be displayed for the row.
  
`@Input() templateRef: TemplateRef<any>` Contains the template to be displayed in the expandable row section when `expandableRows` is `true`.

### Outputs

`@Output() elementDeleted` Emits the element to be deleted when user clicks on Delete Btn.

`@Output() elementView` Emits the element to be inspected when user clicks on View Btn.

`@Output() pagination` Emits the pagination information when the user navigates between pages.

`@Output() sorted` Emits the sorting information when the user sorts a column.


## Example of Use

Example using server side pagination.

```
<rsa-table [columns]="columns" 
           [tableConfiguration]="tableConfiguration"
           [dataSource]="quarantinesDataSource" 
           [serverPagination]="true"
           [totalElements]="quarantinesTotal"
           [templateRef]="columnTemplateRef" 
           (elementDeleted)="delete($event)"
           (pagination)="loadQuarantineInventoryInfo($event)">

        <ng-template #columnTemplateRef let-element='element'>
          <div class="p-4">
            <p><strong>Comment: </strong>{{element.comment}} {{element.product}}</p>
          </div>
        </ng-template>
</rsa-table>
```
