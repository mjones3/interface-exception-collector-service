import {
    booleanAttribute,
    Component,
    EventEmitter,
    Input,
    numberAttribute,
    Output,
    viewChild,
    ViewEncapsulation,
} from '@angular/core';
import { MatPaginator, PageEvent } from '@angular/material/paginator';

@Component({
    selector: 'biopro-paginator',
    templateUrl: './paginator.component.html',
    styleUrl: './paginator.component.scss',
    encapsulation: ViewEncapsulation.None,
    standalone: true,
    imports: [MatPaginator],
})
export class PaginatorComponent {
    @Input({ required: true, transform: numberAttribute }) total: number;
    @Input({ required: true, transform: numberAttribute }) size: number;
    @Input({ transform: booleanAttribute }) showFirstLastButtons? = true;
    @Input({ transform: booleanAttribute }) hidePageSize? = true;
    @Input() pageIndex: number; //TODO: Check if it's needed

    @Output() paginate = new EventEmitter<PageEvent>();

    matPaginatorRef = viewChild(MatPaginator);

    onPaginate(event: PageEvent) {
        this.paginate.emit(event);
    }
}
