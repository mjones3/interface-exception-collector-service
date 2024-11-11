import { provideHttpClient } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute } from '@angular/router';
import { Apollo } from 'apollo-angular';
import { SearchOrderFilterComponent } from './search-order-filter.component';

const SINGLE_SEARCH_FILTER_KEYS: string[] = ['orderNumber'];

describe('SearchOrderFilterComponent', () => {
    let component: SearchOrderFilterComponent;
    let fixture: ComponentFixture<SearchOrderFilterComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [SearchOrderFilterComponent, NoopAnimationsModule],
            providers: [
                provideHttpClient(),
                Apollo,
                {
                    provide: ActivatedRoute,
                    useValue: {
                        snapshot: {
                            data: {
                                searchData: {},
                            },
                        },
                    },
                },
            ],
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(SearchOrderFilterComponent);
        component = fixture.componentInstance;
        component.ngOnInit();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
        expect(component.enableSubmit).toBeFalsy();
    });

    it('should clear form when reset is triggered', () => {
        Object.keys(component.searchForm.controls).forEach((filterKey) => {
            component.searchForm.controls[filterKey].setValue('Test');
            expect(component.enableSubmit).toBeTruthy();

            component.resetFilters();

            expect(component.searchForm.controls[filterKey].value).toBe(null);
            expect(component.enableSubmit).toBeFalsy();
        });
    });

    it('should disable apply button', () => {
        expect(component.enableSubmit).toBeFalsy();
    });

    it('should enable apply button when blood center Id is entered', () => {
        component.searchForm.controls['orderNumber'].setValue('Test');

        expect(component.enableSubmit).toBeTruthy();
    });

    it('should keep only single search field enabled', () => {
        SINGLE_SEARCH_FILTER_KEYS.forEach((singleFilter) => {
            component.resetFilters();
            component.searchForm.controls[singleFilter].setValue('Test');
            component.onChangeSingleSearchFilter(singleFilter);

            expect(component.enableSubmit).toBeTruthy();
            Object.keys(component.searchForm.controls).forEach((filterKey) => {
                expect(component.searchForm.controls[filterKey].enabled).toBe(
                    filterKey === singleFilter
                );
            });
        });
    });

    it('should show filters', () => {
        const toggleFiltersEvent = jest.spyOn(component.toggleFilters, 'emit');

        component.toggleFilter(true);

        expect(toggleFiltersEvent).toHaveBeenNthCalledWith(1, true);
    });

    it('should hide filters', () => {
        const toggleFiltersEvent = jest.spyOn(component.toggleFilters, 'emit');

        component.toggleFilter(false);

        expect(toggleFiltersEvent).toHaveBeenNthCalledWith(1, false);
    });

    it('should apply filters', () => {
        const applySearchFiltersEvent = jest.spyOn(
            component.applySearchFilters,
            'emit'
        );
        const expectedValue = {
            orderNumber: '',
            orderStatus: '',
            orderPriority: '',
            customer: '',
        };

        component.applyFilterSearch();

        expect(applySearchFiltersEvent).toHaveBeenNthCalledWith(
            1,
            expectedValue
        );
    });
});
