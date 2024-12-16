import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PageEvent } from '@angular/material/paginator';
import { Sort } from '@angular/material/sort';
import { createTestContext } from '@testing';
import { TableDataSource } from 'app/shared/models';
import { TableComponent } from './table.component';

interface DataSource extends TableDataSource {
    name: string;
    detail?: string;
}
describe('TableComponent', () => {
    let component: TableComponent<DataSource>;
    let fixture: ComponentFixture<TableComponent<DataSource>>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [TableComponent],
        }).compileComponents();
    });

    beforeEach(() => {
        const testContext =
            createTestContext<TableComponent<DataSource>>(TableComponent);
        fixture = testContext.fixture;
        component = testContext.component;
        component.configuration = {
            columns: [
                {
                    id: 'name',
                    header: 'Name',
                },
            ],
        };
        fixture.componentRef.setInput('dataSource', [
            {
                name: 'Carlos',
                detail: 'Detail',
            },
        ]);
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should emit paginate event', () => {
        const event: PageEvent = {
            length: 10,
            pageIndex: 0,
            pageSize: 10,
        };

        jest.spyOn(component.paginate, 'emit');

        component.onPaginate(event);

        expect(component.paginate.emit).toHaveBeenLastCalledWith(event);
    });

    it('should emit sort', () => {
        const sortable: Sort = {
            active: 'name',
            direction: 'asc',
        };

        jest.spyOn(component.sort, 'emit');

        component.onSort(sortable);

        expect(component.sort.emit).toHaveBeenCalledWith(sortable);
    });

    it('should append expand column', () => {
        component.configuration.expandableKey = 'expandableKey';
        component.setColumnIds();

        expect(component.columnsId).toHaveLength(2);
        expect(component.columnsId.includes('expand')).toBeTruthy();
    });

    it('should append select column', () => {
        component.configuration.selectable = true;
        component.setColumnIds();

        expect(component.columnsId).toHaveLength(2);
        expect(component.columnsId.includes('select')).toBeTruthy();
    });

    it('should show expand all option', () => {
        component.configuration.showExpandAll = true;
        component.configuration.expandableKey = 'detail';

        expect(component.showExpandAll()).toBeTruthy();
    });

    it('should expand all rows', () => {
        jest.spyOn(component.expandingOneOrMoreRows, 'emit');

        component.configuration.expandableKey = 'detail';
        component.expandCollapseAllRows();

        expect(component.expandedAll).toBeTruthy();
        expect(component.dataSource().every((ds) => ds.expanded)).toBeTruthy();
        expect(component.expandingOneOrMoreRows.emit).toHaveBeenCalledWith([
            ...component.dataSource(),
        ]);
    });

    it('should expand a single row when there is an option to expand all', () => {
        component.configuration.showExpandAll = true;
        component.configuration.expandableKey = 'detail';
        component.expandCollapseRow(component.dataSource()[0]);

        expect(component.dataSource()[0].expanded).toBeTruthy();
        expect(component.expandedAll).toBeTruthy();
    });

    it('should expand a single row when there is not an option to expand all', () => {
        component.configuration.expandableKey = 'detail';
        component.expandCollapseRow(component.dataSource()[0]);

        expect(component.expandedElement).toEqual(component.dataSource()[0]);
    });

    it('should return true when the element is expanded', () => {
        expect(
            component.isExpanded(
                component.dataSource()[0],
                component.dataSource()[0]
            )
        ).toBeTruthy();
    });

    it('should check if all is selected', () => {
        component.selection.select(component.dataSource()[0]);

        expect(component.isAllSelected()).toBeTruthy();
    });

    it('should toggle all rows', () => {
        component.toggleAllRows();

        expect(component.selection.selected).toBeDefined();
        expect(component.selection.selected).toHaveLength(1);
    });
});
