import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  Output,
  SimpleChanges,
  TemplateRef,
  ViewChild,
} from '@angular/core';
import { MatPaginator, MatPaginatorIntl } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { ColumnConfig, CustomMatPaginatorIntl, TableConfiguration } from '@rsa/commons';
import { TreoAnimations } from '@treo';
import { isNotNullOrUndefined } from 'codelyzer/util/isNotNullOrUndefined';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'rsa-table',
  exportAs: 'rsaTable',
  templateUrl: './table.component.html',
  styleUrls: ['./table.component.scss'],
  animations: TreoAnimations,
  providers: [{ provide: MatPaginatorIntl, useClass: CustomMatPaginatorIntl }],
})
export class TableComponent implements OnInit, OnChanges, OnDestroy {
  @Input() columns: ColumnConfig[] = [];
  @Input() deleteBtnLabel = 'delete.label';
  @Input() deleteBtnDisabled = false;
  @Input() deactivateButtonId: string;
  @Input() dataSource: any[];
  @Input() serverPagination = false;
  @Input() totalElements: number;
  @Input() tableConfiguration: TableConfiguration = {} as TableConfiguration;
  @Input() templateRef: TemplateRef<any>;
  @Input() btnsTemplateRef: TemplateRef<any>;

  @Output() elementDeleted: EventEmitter<any> = new EventEmitter();
  @Output() elementView: EventEmitter<any> = new EventEmitter();
  @Output() pagination: EventEmitter<any> = new EventEmitter();
  @Output() sorted: EventEmitter<any> = new EventEmitter();

  @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;
  @ViewChild(MatSort, { static: true }) sort: MatSort;

  columnsId: string[];
  tableDataSource: MatTableDataSource<any>;
  expandedElement: any | null;
  private _unsubscribeAll: Subject<any>;

  constructor() {
    this._unsubscribeAll = new Subject();
  }

  ngOnInit(): void {
    this.initTableConfigurations();

    if (this.tableConfiguration.showSorting) {
      this.sort.sortChange.pipe(takeUntil(this._unsubscribeAll)).subscribe(() => {
        if (this.tableConfiguration.showPagination) {
          this.paginator.firstPage();
        }
        this.expandedElement = null;
      });
    }
  }

  initTableConfigurations(): void {
    this.tableDataSource = new MatTableDataSource(this.dataSource);

    if (this.tableConfiguration.showSorting) {
      this.tableDataSource.sort = this.sort;
    }

    this.tableConfiguration.showPagination = isNotNullOrUndefined(this.tableConfiguration.showPagination)
      ? this.tableConfiguration.showPagination
      : true;

    if (this.tableConfiguration.showPagination) {
      if (!this.serverPagination) {
        this.tableDataSource.paginator = this.paginator;
      } else {
        this.paginator.length = this.totalElements;
      }
    }

    this.columnsId = this.columns.map(column => column.columnId);

    if (this.tableConfiguration.showDeleteBtn) {
      this.columnsId.push('delete');
    }

    if (this.tableConfiguration.showViewBtn) {
      this.columnsId.push('view');
    }

    if (this.tableConfiguration.expandableRows) {
      this.columnsId.unshift('detail');
    }

    if (isNotNullOrUndefined(this.btnsTemplateRef)) {
      this.columnsId.push('customActions');
    }
  }

  onSort(sortInfo): void {
    this.sorted.emit(sortInfo);
  }

  onDelete(element): void {
    this.elementDeleted.emit(element);
  }

  onView(element) {
    this.elementView.emit(element);
  }

  toggleDetails(element): void {
    this.expandedElement = this.expandedElement === element ? null : element;
  }

  public goToFirstPage() {
    this.paginator.firstPage();
  }

  // Updating Table Data Source after Data has changed from parent component
  ngOnChanges(changes: SimpleChanges): void {
    if (changes.dataSource && changes.dataSource.previousValue) {
      this.tableDataSource.data = changes.dataSource.currentValue;
      this.tableDataSource._updateChangeSubscription();
    }
  }

  // Unsubscribe from all subscriptions
  ngOnDestroy(): void {
    this._unsubscribeAll.next();
    this._unsubscribeAll.complete();
  }

  paginate(event) {
    this.pagination.emit(event);
  }
}
