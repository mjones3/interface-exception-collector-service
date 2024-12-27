import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { PageEvent } from '@angular/material/paginator';
import { createTestContext } from '@testing';
import { PaginatorComponent } from './paginator.component';

describe('PaginatorComponent', () => {
    let component: PaginatorComponent;
    let fixture: ComponentFixture<PaginatorComponent>;

    beforeEach(waitForAsync(() => {
        TestBed.configureTestingModule({
            imports: [PaginatorComponent],
        }).compileComponents();
    }));

    beforeEach(() => {
        const testContext =
            createTestContext<PaginatorComponent>(PaginatorComponent);
        fixture = testContext.fixture;
        component = testContext.component;
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

        expect(component.paginate.emit).toHaveBeenCalledWith(event);
    });
});
