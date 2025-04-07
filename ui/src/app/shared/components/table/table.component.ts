import {
    animate,
    state,
    style,
    transition,
    trigger,
} from '@angular/animations';
import { SelectionModel } from '@angular/cdk/collections';
import { NgClass, NgTemplateOutlet } from '@angular/common';
import {
    Component,
    OnInit,
    TemplateRef,
    ViewEncapsulation,
    booleanAttribute,
    computed,
    effect,
    input,
    numberAttribute,
    output,
    viewChild,
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatIconModule } from '@angular/material/icon';
import { PageEvent } from '@angular/material/paginator';
import {
    MatSort,
    MatSortModule,
    MatSortable,
    Sort,
} from '@angular/material/sort';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { FuseCardComponent } from '@fuse/components/card';
import {
    TableConfiguration,
    TableDataSource,
} from 'app/shared/models/table.model';
import { merge } from 'lodash-es';
import { MenuComponent } from '../menu/menu.component';
import { PaginatorComponent } from '../paginator/paginator.component';

@Component({
    selector: 'biopro-table',
    templateUrl: './table.component.html',
    styleUrls: ['./table.component.scss'],
    encapsulation: ViewEncapsulation.None,
    animations: [
        trigger('detailExpand', [
            state('collapsed,void', style({ height: '0px', minHeight: '0' })),
            state('expanded', style({ height: '*' })),
            transition(
                'expanded <=> collapsed',
                animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')
            ),
        ]),
    ],
    standalone: true,
    imports: [
        MatTableModule,
        MatButtonModule,
        MatIconModule,
        MatCheckboxModule,
        MatSortModule,
        NgTemplateOutlet,
        NgClass,
        FuseCardComponent,
        MenuComponent,
        PaginatorComponent,
    ],
})
export class TableComponent<T extends TableDataSource = TableDataSource>
    implements OnInit
{
    private _defaultConfig: TableConfiguration = {
        menus: [],
        columns: [],
        pageSize: 20,
        showPagination: true,
        showSorting: true,
    };

    dataSource = input.required<T[]>();
    configuration = input<TableConfiguration, unknown>(this._defaultConfig, {
        transform: (config: TableConfiguration) =>
            merge({}, this._defaultConfig, config),
    });
    serverPagination = input(false, { transform: booleanAttribute });
    totalElements = input(0, { transform: numberAttribute });
    expandTemplateRef = input<TemplateRef<Element>>();
    footerTemplateRef = input<TemplateRef<Element>>();
    tableId = input.required<string>();
    stickyHeader = input(true, { transform: booleanAttribute });
    tableNoResultsMessage = input('No Results Found');
    noDataRowMessage = computed(() =>
        this.totalElements() === 0 ? this.tableNoResultsMessage() : 'Loading...'
    );
    pageIndex = input(0);
    defaultSort = input<MatSortable>();
    sortingDataAccessor = input<
        (data: T, sortHeaderId: string) => string | number
    >((data, sortHeaderId) => {
        return typeof data[sortHeaderId] === 'string'
            ? data[sortHeaderId].toLowerCase()
            : data[sortHeaderId];
    });
    paginate = output<PageEvent>();
    sort = output<Sort>();
    expandingOneOrMoreRows = output<T | T[]>(); //Lazy loading for the expandable content

    matSortRef = viewChild(MatSort);
    paginatorRef = viewChild(PaginatorComponent);

    columnsId = computed(() => {
        const ids = this.configuration().columns.map((col) => col.id);
        if (this.configuration().expandableKey) {
            ids.unshift('expand');
        }
        if (this.configuration().selectable) {
            ids.unshift('select');
        }
        return ids;
    });
    expandedAll: boolean;
    expandedElement: T;
    selection = new SelectionModel<T>(true, []);
    tableDataSource: MatTableDataSource<T>;

    constructor() {
        effect(
            () => {
                this.tableDataSource.data = this.dataSource();

                if (this.configuration().showPagination) {
                    if (!this.serverPagination()) {
                        this.tableDataSource.paginator =
                            this.paginatorRef().matPaginatorRef();
                    } else {
                        this.paginatorRef().matPaginatorRef().length =
                            this.totalElements();
                    }
                }

                if (this.configuration().showSorting) {
                    this.tableDataSource.sort = this.matSortRef();
                    this.tableDataSource.sortingDataAccessor =
                        this.sortingDataAccessor();
                }
            },
            { allowSignalWrites: true }
        );
    }

    ngOnInit(): void {
        this.tableDataSource = new MatTableDataSource(this.dataSource());
        this.expandedAll = (this.dataSource()?.findIndex((element) => element.expanded) ?? -1) !== -1;
    }

    onPaginate(event: PageEvent) {
        this.paginate.emit(event);
    }

    onSort(event: Sort) {
        if (this.configuration().showPagination) {
            this.paginatorRef().matPaginatorRef().firstPage();
        }
        this.expandedElement = null;
        this.sort.emit(event);
    }

    //#region Expand

    showExpandAll() {
        return (
            this.configuration().showExpandAll &&
            this.dataSource().some((element) =>
                this.hasExpandableContent(element)
            )
        );
    }

    expandCollapseAllRows() {
        this.expandedAll = !this.expandedAll;
        this.dataSource().forEach((element) => {
            if (this.hasExpandableContent(element)) {
                element.expanded = this.expandedAll;
            }
        });
        if (this.expandedAll) {
            this.expandingOneOrMoreRows.emit([...this.dataSource()]);
        }
    }

    expandCollapseRow(element: T) {
        if (this.hasExpandableContent(element)) {
            if (this.configuration().showExpandAll) {
                element.expanded = !element.expanded;
                if (element.expanded) {
                    this.expandingOneOrMoreRows.emit(element);
                }
                this.expandedAll = (this.dataSource()?.findIndex((element) => element.expanded) ?? -1) !== -1;
            } else {
                this.expandedElement =
                    this.expandedElement === element ? null : element;
                if (!this.expandedElement) {
                    this.expandingOneOrMoreRows.emit(element);
                }
            }
        }
    }

    isExpanded(element: T, expandedElement: T) {
        return (
            (this.configuration().showExpandAll && element.expanded) ||
            element === expandedElement
        );
    }

    hasExpandableContent(element: T) {
        const content = element[this.configuration().expandableKey];
        if (
            content &&
            ((Array.isArray(content) && content.length) ||
                !Array.isArray(content))
        ) {
            return true;
        }

        return false;
    }
    //#endregion

    //#region Select

    isAllSelected() {
        const numSelected = this.selection.selected.length;
        const numRows = this.dataSource().length;
        return numSelected === numRows;
    }

    toggleAllRows() {
        if (this.isAllSelected()) {
            this.selection.clear();
            return;
        }

        this.selection.select(...this.dataSource());
    }

    checkboxLabel(element?: T, index?: number): string {
        if (!element) {
            return `${this.isAllSelected() ? 'deselect' : 'select'} all`;
        }
        return `${this.selection.isSelected(element) ? 'deselect' : 'select'} row ${index + 1}`;
    }
    //#endregion
}
