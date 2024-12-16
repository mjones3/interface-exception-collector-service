# Paginator Component

The Paginator component provides a navigation for paged information, typically used with a table.

## Properties

### Inputs

`@Input({ required: true, transform: numberAttribute }) total: number` The length of the total number of items that are being paginated.

`@Input({ required: true, transform: numberAttribute }) size: number` Number of items to display on a page.

`@Input({ transform: booleanAttribute }) showFirstLastButtons?: boolean` Whether to show the first/last buttons UI to the user. Default value `true`.

`@Input({ transform: booleanAttribute }) hidePageSize?: boolean` Whether to hide the page size selection UI from the user. Default value `true`.

### Outputs

`@Output() paginate` Emits the pagination information when the user navigates between pages.

## Example of Use

Examples

```
<biopro-paginator
    [total]="totalElements"
    [pageIndex]="pageIndex"
    [size]="pageSize"
    (paginate)="onPaginate($event)"
></biopro-paginator>
```
