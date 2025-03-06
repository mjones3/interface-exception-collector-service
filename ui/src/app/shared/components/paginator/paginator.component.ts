import {
    booleanAttribute,
    Component,
    input,
    numberAttribute,
    output,
    viewChild,
    ViewEncapsulation,
} from '@angular/core';
import { MatPaginator, PageEvent } from '@angular/material/paginator';

@Component({
    selector: 'biopro-paginator',
    templateUrl: './paginator.component.html',
    styleUrls: ['./paginator.component.scss'],
    encapsulation: ViewEncapsulation.None,
    standalone: true,
    imports: [MatPaginator],
})
export class PaginatorComponent {
    total = input.required({ transform: numberAttribute });
    size = input.required({ transform: numberAttribute });
    showFirstLastButtons = input(true, {
        transform: booleanAttribute,
    });
    hidePageSize = input(true, { transform: booleanAttribute });
    pageIndex = input(0, { transform: numberAttribute });

    paginate = output<PageEvent>();

    matPaginatorRef = viewChild(MatPaginator);

    onPaginate(event: PageEvent) {
        this.paginate.emit(event);
    }
}
