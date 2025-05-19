import { CommonModule } from '@angular/common';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatNativeDateModule } from '@angular/material/core';
import { MatDialog } from '@angular/material/dialog';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ApolloError, NetworkStatus } from '@apollo/client';
import { ProcessHeaderService } from '@shared';
import { CookieService } from 'ngx-cookie-service';
import { ToastrModule, ToastrService } from 'ngx-toastr';
import { of, throwError } from 'rxjs';
import { DateRangePickerComponent } from '../../../../shared/components/date-range-picker/date-range-picker.component';
import { MultipleSelectComponent } from '../../../../shared/components/multiple-select/multiple-select.component';
import { EMPTY_PAGE } from '../../../../shared/models/page.model';
import { RecoveredPlasmaService } from '../../services/recovered-plasma.service';
import { CreateShipmentComponent } from '../create-shipment/create-shipment.component';
import { SearchShipmentComponent } from './search-shipment.component';

describe('SearchShipmentComponent', () => {
    let component: SearchShipmentComponent;
    let fixture: ComponentFixture<SearchShipmentComponent>;
    let mockToastrService: jest.Mocked<ToastrService>;
    let mockMatDialog: jest.Mocked<MatDialog>;
    let mockRecoveredPlasmaService: jest.Mocked<RecoveredPlasmaService>;
    let mockCookieService: jest.Mocked<CookieService>;

    beforeEach(async () => {
        mockToastrService = {
            success: jest.fn(),
            error: jest.fn(),
        } as any;

        mockMatDialog = {
            open: jest.fn(),
        } as any;

        mockRecoveredPlasmaService = {
            searchRecoveredPlasmaShipments: jest.fn(),
            findAllLocations: jest.fn(),
            findAllCustomers: jest.fn(),
            findAllLookupsByType: jest.fn(),
            checkRecoveredPlasmaFacility: jest.fn(),
        } as any;

        mockCookieService = {
            get: jest.fn(),
        } as any;

        await TestBed.configureTestingModule({
            imports: [
                SearchShipmentComponent,
                NoopAnimationsModule,
                CommonModule,
                ReactiveFormsModule,
                MultipleSelectComponent,
                DateRangePickerComponent,
                MatNativeDateModule,
                ToastrModule.forRoot()
            ],
            providers: [
                ProcessHeaderService,
                { provide: ToastrService, useValue: mockToastrService },
                { provide: MatDialog, useValue: mockMatDialog },
                {
                    provide: RecoveredPlasmaService,
                    useValue: mockRecoveredPlasmaService,
                },
                { provide: CookieService, useValue: mockCookieService },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(SearchShipmentComponent);
        component = fixture.componentInstance;
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    describe('ngOnInit', () => {
        beforeEach(() => {
            mockRecoveredPlasmaService.searchRecoveredPlasmaShipments.mockReturnValue(
                of({
                    data: {
                        searchShipment: {
                            data: EMPTY_PAGE,
                            notifications: [],
                        },
                    },
                    loading: false,
                    networkStatus: NetworkStatus.ready,
                })
            );
            mockRecoveredPlasmaService.findAllLocations.mockReturnValue(
                of({
                    data: { findAllLocations: [] },
                    loading: false,
                    networkStatus: NetworkStatus.ready,
                })
            );
            mockRecoveredPlasmaService.findAllCustomers.mockReturnValue(
                of({
                    data: { findAllCustomers: [] },
                    loading: false,
                    networkStatus: NetworkStatus.ready,
                })
            );
            mockRecoveredPlasmaService.findAllLookupsByType.mockReturnValue(
                of({
                    data: { findAllLookupsByType: [] },
                    loading: false,
                    networkStatus: NetworkStatus.ready,
                })
            );
            mockRecoveredPlasmaService.checkRecoveredPlasmaFacility.mockReturnValue(
                of(true)
            );
        });

        it('should load initial data on init', () => {
            component.ngOnInit();
            expect(
                mockRecoveredPlasmaService.searchRecoveredPlasmaShipments
            ).toHaveBeenCalled();
            expect(
                mockRecoveredPlasmaService.findAllLocations
            ).toHaveBeenCalled();
            expect(
                mockRecoveredPlasmaService.findAllCustomers
            ).toHaveBeenCalled();
            expect(
                mockRecoveredPlasmaService.findAllLookupsByType
            ).toHaveBeenCalledTimes(2);
            expect(
                mockRecoveredPlasmaService.checkRecoveredPlasmaFacility
            ).toHaveBeenCalled();
        });

        it('should handle errors during data loading', () => {
            const error = new ApolloError({
                graphQLErrors: [{ message: 'Test error' }],
            });
            mockRecoveredPlasmaService.searchRecoveredPlasmaShipments.mockReturnValue(
                throwError(() => error)
            );

            component.ngOnInit();
            expect(mockToastrService.error).toHaveBeenCalled();
        });
    });

    describe('applyFilterSearch', () => {
        it('should update filter and reload data', () => {
            const searchCriteria = { shipmentNumber: '123' };
            mockRecoveredPlasmaService.searchRecoveredPlasmaShipments.mockReturnValue(
                of({
                    data: {
                        searchShipment: {
                            data: EMPTY_PAGE,
                            notifications: [],
                        },
                    },
                    loading: false,
                    networkStatus: NetworkStatus.ready,
                })
            );

            component.applyFilterSearch(searchCriteria);

            expect(component.isFilterToggled).toBeFalsy();
            expect(
                mockRecoveredPlasmaService.searchRecoveredPlasmaShipments
            ).toHaveBeenCalledWith(searchCriteria);
        });
    });

    describe('openCreateShipment', () => {
        it('should open create shipment dialog', () => {
            component.openCreateShipment();

            expect(mockMatDialog.open).toHaveBeenCalledWith(
                CreateShipmentComponent,
                {
                    width: '50rem',
                    disableClose: true,
                }
            );
        });
    });

    describe('handlePagination', () => {
        it('should update filter with new page number and reload data', () => {
            const pageEvent = { pageIndex: 1, pageSize: 10, length: 100 };
            mockRecoveredPlasmaService.searchRecoveredPlasmaShipments.mockReturnValue(
                of({
                    data: {
                        searchShipment: { data: EMPTY_PAGE, notifications: [] },
                    },
                    loading: false,
                    networkStatus: NetworkStatus.ready,
                })
            );

            component.handlePagination(pageEvent);

            expect(
                mockRecoveredPlasmaService.searchRecoveredPlasmaShipments
            ).toHaveBeenCalledWith(expect.objectContaining({ pageNumber: 1 }));
        });
    });

    describe('resetFilterSearch', () => {
        it('should clear current filter', () => {
            component.resetFilterSearch();
            expect(component.currentFilter).toEqual({});
        });
    });
});
