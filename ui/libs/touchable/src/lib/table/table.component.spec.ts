import { SimpleChange } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { MaterialModule } from '@rsa/material';
import { createTestContext } from '@rsa/testing';
import { addTestingIconsMock } from '../../shared/testing/mocks/data/icons.mock';
import { TableComponent } from './table.component';

describe('TableComponent', () => {
  let component: TableComponent;
  let fixture: ComponentFixture<TableComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [TableComponent],
      imports: [
        NoopAnimationsModule,
        MaterialModule,
        TranslateModule.forRoot({
          loader: {
            provide: TranslateLoader,
            useClass: TranslateFakeLoader
          }
        })
      ]
    });
    const testContext = createTestContext<TableComponent>(TableComponent);
    fixture = testContext.fixture;
    component = testContext.component;
    addTestingIconsMock(testContext);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should enable sorting' +
    '', () => {
    component.tableConfiguration = {
      showSorting: true
    };
    component.ngOnInit();
  });

  it('should emit sort', () => {
    spyOn(component.sorted, 'emit').and.callThrough();
    component.onSort('sort');
    expect(component.sorted.emit).toHaveBeenCalledWith('sort');
  });

  it('should emit pagination', () => {
    spyOn(component.pagination, 'emit').and.callThrough();
    component.paginate({});
    expect(component.pagination.emit).toHaveBeenCalled();
  });

  it('should emit element deleted', () => {
    spyOn(component.elementDeleted, 'emit').and.callThrough();
    component.onDelete('1');
    expect(component.elementDeleted.emit).toHaveBeenCalledWith('1');
  });

  it('should emit view', () => {
    spyOn(component.elementView, 'emit').and.callThrough();
    component.onView('1');
    expect(component.elementView.emit).toHaveBeenCalledWith('1');
  });


  it('should show delete button', () => {
    component.tableConfiguration = {
      showDeleteBtn: true
    };
    component.ngOnInit();
    expect(component.columnsId.includes('delete')).toEqual(true);
  });

  it('should show view button', () => {
    component.tableConfiguration = {
      showViewBtn: true
    };
    component.ngOnInit();
    expect(component.columnsId.includes('view')).toEqual(true);
  });

  it('should show expandable Rows', () => {
    component.tableConfiguration = {
      expandableRows: true
    };
    component.ngOnInit();
    expect(component.columnsId.includes('detail')).toEqual(true);
  });

  it('should fill data table on change', () => {
    const dataSource = [{ value: 'value' }];
    component.ngOnChanges({
      dataSource: new SimpleChange([], dataSource, false)
    });
    expect(component.tableDataSource.data).toEqual(dataSource);
  });

  it('should get value ', () => {
    const dataSource = [{ value: 'value' }];
    component.totalElements = 1;
    component.dataSource = dataSource;
    component.tableConfiguration = {
      showPagination: true
    };
    component.serverPagination = true;
    fixture.detectChanges();
    component.ngOnInit();
    expect(component.paginator.length).toEqual(component.totalElements);
  });

});
