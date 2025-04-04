import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { provideMockStore } from '@ngrx/store/testing';
import { FilterShipmentComponent } from './filter-shipment.component';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { FiltersComponent } from '../../../../shared/components/filters/filters.component';
import { MultipleSelectComponent } from '../../../../shared/components/multiple-select/multiple-select.component';
import { DateRangePickerComponent } from '../../../../shared/components/date-range-picker/date-range-picker.component';
import { MatNativeDateModule } from '@angular/material/core';

describe('FilterShipmentComponent', () => {
    let component: FilterShipmentComponent;
    let fixture: ComponentFixture<FilterShipmentComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                FilterShipmentComponent,
                NoopAnimationsModule,
                CommonModule,
                ReactiveFormsModule,
                MatButtonModule,
                MatDatepickerModule,
                MatDividerModule,
                MatIconModule,
                MatInputModule,
                MatSelectModule,
                FiltersComponent,
                MultipleSelectComponent,
                DateRangePickerComponent,
                MatNativeDateModule,
            ],
            providers: [provideHttpClient(), provideMockStore({}), FormBuilder],
        }).compileComponents();

        fixture = TestBed.createComponent(FilterShipmentComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    describe('Form Initialization and Validation', () => {
        it('should initialize with empty form values', () => {
            expect(component.form.get('shipmentNumber').value).toBeNull();
            expect(component.form.get('locationCode').value).toEqual([]);
            expect(component.form.get('shipmentStatus').value).toEqual([]);
            expect(component.form.get('customers').value).toEqual([]);
            expect(component.form.get('productTypes').value).toEqual([]);
            expect(
                component.form.get('transportationReferenceNumber').value
            ).toBeNull();
        });

        it('should validate shipmentNumber max length', () => {
            const shipmentNumberControl = component.form.get('shipmentNumber');
            shipmentNumberControl.setValue('a'.repeat(51));
            expect(shipmentNumberControl.errors?.['maxlength']).toBeTruthy();

            shipmentNumberControl.setValue('a'.repeat(50));
            expect(shipmentNumberControl.errors).toBeNull();
        });
    });

    describe('Utility Functions', () => {
        it('should format date to ISO string', () => {
            const date = new Date('2024-01-15T12:00:00Z');
            expect(component.formatToISO(date)).toBe('2024-01-15');
        });

        it('should handle invalid date for ISO formatting', () => {
            const invalidDate = 'invalid-date';
            expect(() => component.formatToISO(invalidDate)).toThrow();
        });

        it('should check if a field is informed', () => {
            component.form.patchValue({
                shipmentStatus: ['STATUS'],
                locationCode: [],
                productTypes: ['TYPE1'],
            });

            expect(component.isFieldInformed('shipmentStatus')).toBe(true);
            expect(component.isFieldInformed('locationCode')).toBe(false);
            expect(component.isFieldInformed('productTypes')).toBe(true);
        });
    });

    describe('Form Operations', () => {
        it('should calculate total fields informed correctly', () => {
            component.form.patchValue({
                shipmentStatus: ['STATUS'],
                locationCode: ['LOC1'],
                productTypes: ['TYPE1'],
                customers: [],
            });
            expect(component.totalFieldsInformed()).toBe(3);
        });

        it('should reset form correctly', () => {
            const emitSpy = jest.spyOn(component.onResetFilters, 'emit');

            component.form.patchValue({
                shipmentStatus: ['STATUS'],
                locationCode: ['LOC1'],
                productTypes: ['TYPE1'],
            });

            component.resetFilters();

            expect(component.form.get('shipmentStatus').value).toBeNull();
            expect(component.form.get('locationCode').value).toBeNull();
            expect(component.form.get('productTypes').value).toBeNull();
            expect(emitSpy).toHaveBeenCalledWith(null);
        });
    });

    describe('Date Range Handling', () => {
        it('should detect valid date range', () => {
            const dateRange = {
                start: new Date('2024-01-01'),
                end: new Date('2024-01-31'),
            };
            component.form.get('shipmentDate').patchValue(dateRange);

            expect(component.isDateRangeInformed(dateRange)).toBe(true);
            expect(component.isInvalidDateRangeInformed('shipmentDate')).toBe(
                false
            );
        });

        it('should handle invalid date range', () => {
            const invalidDateRange = {
                start: new Date('2024-01-31'),
                end: new Date('2024-01-01'),
            };
            component.form.get('shipmentDate').patchValue(invalidDateRange);

            expect(component.isInvalidDateRangeInformed('shipmentDate')).toBe(
                true
            );
        });
    });

    describe('Form State Management', () => {
        it('should handle form state correctly', () => {
            component.form.disable();
            component.resetFilters();
            expect(component.form.enabled).toBe(true);

            const formControls = Object.keys(component.form.controls);
            formControls.forEach((controlName) => {
                expect(component.form.get(controlName).enabled).toBe(true);
            });
        });

        it('should track total fields applied', () => {
            component.form.patchValue({
                shipmentStatus: ['STATUS'],
                locationCode: ['LOC1'],
                productTypes: ['TYPE1'],
            });

            component.applyFilterSearch();
            expect(component.totalFieldsApplied()).toBe(3);

            component.resetFilters();
            expect(component.totalFieldsApplied()).toBe(0);
        });
    });

    describe('Filter Operations', () => {
        it('should apply filter search with correct criteria', () => {
            const emitSpy = jest.spyOn(component.onApplySearchFilters, 'emit');

            component.form.patchValue({
                shipmentStatus: ['STATUS'],
                locationCode: ['LOC1'],
                productTypes: ['TYPE1'],
                shipmentDate: {
                    start: new Date('2024-01-01'),
                    end: new Date('2024-01-31'),
                },
            });

            component.applyFilterSearch();

            const emittedCriteria = emitSpy.mock.calls[0][0];
            expect(emittedCriteria.shipmentStatus).toStrictEqual(['STATUS']);
            expect(emittedCriteria.locationCode).toEqual(['LOC1']);
            expect(emittedCriteria.productTypes).toEqual(['TYPE1']);
            expect(emittedCriteria.shipmentDateFrom).toBe('2024-01-01');
            expect(emittedCriteria.shipmentDateTo).toBe('2024-01-31');
        });

        it('should handle select-all value correctly', () => {
            const result = component.dropSelectAllOptionFrom([
                'select-all',
                'option1',
                'option2',
            ]);
            expect(result).toEqual(['option1', 'option2']);
        });
    });
});
