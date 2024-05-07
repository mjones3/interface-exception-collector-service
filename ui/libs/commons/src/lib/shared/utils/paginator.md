## Paginator

### ResponseWithPagination Decorator (@ResponseWithPagination())
This decorator can be used over the method that return the data that needs to be paginated, 
basically what it does is to change the descriptor value of the method that is decorated 
read the pagination header and return instead of the actual response an object containing the following:
1. `body: any[]`                Array of items 
2. `links: PaginationLinks`     Pagination links
2. `total: number`              Total amount of items 

##### Example: 
```ts
@Injectable({providedIn: 'root'})
export class DataService {
  url = 'url';

  constructor(private httpClient: HttpClient) {}

  @ResponseWithPagination()
  getData(pageable?: Pageable): Observable<IResponseWithPagination | any> {
    const {page, size} = pageable;
    const options: any = {observe: 'response'};
    // Second and next pages show the loader in the container section only
    if (pageable && pageable.page > 0) {
      // Container to show the loader (Replace selector)
      const selector = 'div.container';
      options.headers = getLoaderHeaders(selector);
    }
    return this.httpClient.get(addQueryParamsToUrl(this.url, {page, size}), options);
  }

}
```


### Use of Paginator in Components

##### Example: 
```ts
@Component({
    template: `
       <div class="flex flex-row items-baseline">
         <!-- Other 'rsa-select-options-picker' inputs omitted for brevity of this example -->
         <rsa-select-options-picker [options]="options" 
            [pageable]="pageable" (pageableChange)="onPageChange($event)"></rsa-select-options-picker>
       </div>`
})
export class TargetComponent {
    
    constructor(private dataService: DataService) {}
    
    loadData(pageable?: Pageable): void {
        this.dataService.getData(pageable)
            .subscribe((data: IResponseWithPagination) => {
                this.pageable = {page: pageable?.page, totalItems: +data.total, size: pageable?.size};
                this.options = [...(this.options || []), ...data?.body];
            });
    }

    onPageChange($event: Pageable) {
        this.loadData($event);
    }
}
```
