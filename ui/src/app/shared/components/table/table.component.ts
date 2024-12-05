import {
    animate,
    state,
    style,
    transition,
    trigger,
} from '@angular/animations';
import { SelectionModel } from '@angular/cdk/collections';
import { CommonModule, NgClass, NgTemplateOutlet } from '@angular/common';
import {
    Component,
    EventEmitter,
    Input,
    OnInit,
    Output,
    TemplateRef,
    ViewEncapsulation,
    booleanAttribute,
    effect,
    input,
    numberAttribute,
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
import { ProgressBarComponent } from 'app/progress-bar/progress-bar.component';
import {
    AngularMaterialTableConfiguration,
    TableDataSource,
} from 'app/shared/models/table.model';
import { ProductIconsService } from 'app/shared/services/product-icon.service';
import { merge } from 'lodash-es';
import { MenuComponent } from '../menu/menu.component';
import { PaginatorComponent } from '../paginator/paginator.component';

@Component({
    selector: 'biopro-table',
    templateUrl: './table.component.html',
    styleUrl: './table.component.scss',
    standalone: true,
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
        CommonModule,
        MatButtonModule,
        ProgressBarComponent,
    ],
})
export class TableComponent<T extends TableDataSource = TableDataSource>
    implements OnInit
{
    private _defaultConfig: AngularMaterialTableConfiguration = {
        menus: [],
        columns: [],
        pageSize: 20,
        showPagination: true,
        showSorting: true,
    };
    private _configuration: AngularMaterialTableConfiguration;

    dataSource = input.required<T[]>();

    @Input() set configuration(config: AngularMaterialTableConfiguration) {
        this._configuration = merge({}, this._defaultConfig, config);
        this.setColumnIds();
    }
    get configuration() {
        return this._configuration;
    }

    @Input({ transform: booleanAttribute }) serverPagination = false;
    @Input({ transform: numberAttribute }) totalElements: number;
    @Input() expandTemplateRef?: TemplateRef<Element>;
    @Input() footerTemplateRef?: TemplateRef<Element>;
    @Input() tableNoResultsMessage?: string = 'No Results Found.';
    @Input() pageIndex = 0; //TODO: Check if it's really necessary
    @Input() defaultSort?: MatSortable;
    @Input() btnTemplateRef?: TemplateRef<Element>;

    @Output() paginate = new EventEmitter<PageEvent>();
    @Output() sort = new EventEmitter<Sort>();
    @Output() expandingOneOrMoreRows = new EventEmitter<T | T[]>(); //Lazy loading for the expandable content

    matSortRef = viewChild(MatSort);
    paginatorRef = viewChild(PaginatorComponent);

    columnsId: string[] = [];
    expandedAll: boolean;
    expandedElement: T;
    selection = new SelectionModel<T>(true, []);
    tableDataSource: MatTableDataSource<T>;

    constructor(private productIconService: ProductIconsService) {
        effect(
            () => {
                this.tableDataSource.data = this.dataSource();

                if (this.configuration.showPagination) {
                    if (!this.serverPagination) {
                        this.tableDataSource.paginator =
                            this.paginatorRef().matPaginatorRef();
                    } else {
                        this.paginatorRef().matPaginatorRef().length =
                            this.totalElements;
                    }
                }

                if (this.configuration.showSorting) {
                    this.tableDataSource.sort = this.matSortRef();
                    if (this.defaultSort && this.dataSource?.length) {
                        this.tableDataSource.sort.sort(this.defaultSort);
                    }
                }
            },
            { allowSignalWrites: true }
        );
    }

    ngOnInit(): void {
        this.tableDataSource = new MatTableDataSource(this.dataSource());
        this.expandedAll =
            this.dataSource().findIndex((element) => element.expanded) !== -1;
    }

    onPaginate(event: PageEvent) {
        this.paginate.emit(event);
    }

    onSort(event: Sort) {
        if (this.configuration.showPagination) {
            this.paginatorRef().matPaginatorRef().firstPage();
        }
        this.expandedElement = null;
        this.sort.emit(event);
    }

    setColumnIds() {
        this.columnsId = this.configuration.columns.map((col) => col.id);
        if (this.configuration.expandableKey) {
            this.columnsId.unshift('expand');
        }
        if (this.configuration.selectable) {
            this.columnsId.unshift('select');
        }
    }

    //#region Expand

    showExpandAll() {
        return (
            this.configuration.showExpandAll &&
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
            if (this.configuration.showExpandAll) {
                element.expanded = !element.expanded;
                if (element.expanded) {
                    this.expandingOneOrMoreRows.emit(element);
                }
                this.expandedAll =
                    this.dataSource().findIndex(
                        (element) => element.expanded
                    ) !== -1;
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
            (this.configuration.showExpandAll && element.expanded) ||
            element === expandedElement
        );
    }

    hasExpandableContent(element: T) {
        const content = element[this.configuration.expandableKey];
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
    getIcon(productFamily: string) {
        return this.productIconService.getIconByProductFamily(productFamily);
    }
}
