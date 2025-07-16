import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { ImportsEnterShipmentInformationComponent } from './imports-enter-shipment-information.component';
import { Router } from '@angular/router';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ReceivingService } from '../../service/receiving.service';
import { CookieService } from 'ngx-cookie-service';
import { of, throwError } from 'rxjs';
import { ApolloError, ApolloQueryResult } from '@apollo/client';
import { Cookie } from '../../../../shared/types/cookie.enum';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ShippingInformationDTO } from '../../graphql/query-definitions/imports-enter-shipping-information.graphql';
import { UseCaseResponseDTO } from '../../../../shared/models/use-case-response.dto';
import { MatIconTestingModule } from '@angular/material/icon/testing';
import { provideMockStore } from '@ngrx/store/testing';
import { ToastrService } from 'ngx-toastr';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { DateTime } from 'luxon';
import { DeviceIdValidator } from 'app/shared/forms/device-id.validator';

describe('ImportsEnterShipmentInformationComponent', () => {
    let component: ImportsEnterShipmentInformationComponent;
    let fixture: ComponentFixture<ImportsEnterShipmentInformationComponent>;
    let mockRouter: jest.Mocked<Router>;
    let mockToastr: jest.Mocked<ToastrService>;
    let mockReceivingService: jest.Mocked<ReceivingService>;
    let mockCookieService: jest.Mocked<CookieService>;

    const mockLookups = [
        { id: '1', descriptionKey: 'Category 1', code: 'CAT1' },
        { id: '2', descriptionKey: 'Category 2', code: 'CAT2' }
    ];

    const mockShippingInfo = {
        transitTimeZoneList: [
            { descriptionKey: 'UTC' },
            { descriptionKey: 'GMT' }
        ],
        visualInspectionList: [],
        defaultTimeZone: '',
        displayTransitInformation: true,
        productCategory: "ROOM_TEMPERATURE",
        temperatureUnit: "celsius",
        displayTemperature: true
    };

    beforeEach(async () => {
        mockRouter = { 
            navigate: jest.fn(), 
            navigateByUrl: jest.fn()
        } as any;
        mockToastr = {
            show: jest.fn(),
            success: jest.fn(),
            error: jest.fn(),
            warning: jest.fn()
        } as any;
        mockReceivingService = {
            findAllLookupsByType: jest.fn().mockReturnValue(of({ data: { findAllLookupsByType: mockLookups } })),
            queryEnterShippingInformation: jest.fn().mockReturnValue(of({
                data: {
                    enterShippingInformation: {
                        data: mockShippingInfo,
                        notifications: []
                    }
                }
            })),
            validateDevice: jest.fn().mockReturnValue(of({ data: { validateDevice: {}}})),
            validateTemperature: jest.fn().mockReturnValue(of({ data: { validateTemperature: {}}})),
            validateTransitTime: jest.fn().mockReturnValue(of({ data: { validateTransitTime: {}}})),
            createImport: jest.fn().mockReturnValue(of({ data: { createImport: {}}})),
        } as any;
        mockCookieService = {
            get: jest.fn().mockReturnValue('testFacility')
        } as any;

        jest.spyOn(DeviceIdValidator, 'asyncValidatorUsing').mockReturnValue(() => of(null));

        await TestBed.configureTestingModule({
            imports: [
                ImportsEnterShipmentInformationComponent,
                ReactiveFormsModule,
                BrowserAnimationsModule,
                MatIconTestingModule,
                MatDatepickerModule,
                MatNativeDateModule,
            ],
            providers: [
                FormBuilder,
                provideMockStore({
                    initialState: {
                        auth: {
                            id: 'testEmployeeId'
                        }
                    }
                }),
                { provide: Router, useValue: mockRouter },
                { provide: ToastrService, useValue: mockToastr },
                { provide: ReceivingService, useValue: mockReceivingService },
                { provide: CookieService, useValue: mockCookieService }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(ImportsEnterShipmentInformationComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should initialize form with empty values', () => {
        expect(component.form.get('temperatureProductCategory').value).toBeNull();
        expect(component.form.get('comments').value).toBeNull();
    });

    it('should fetch lookups on init', () => {
        component.ngOnInit();
        expect(mockReceivingService.findAllLookupsByType).toHaveBeenCalledWith('TEMPERATURE_PRODUCT_CATEGORY');
        expect(component.productCategoryLookupsSignal()).toEqual(mockLookups);
    });

    it('should fetch shipping information when selecting category', () => {
        const productCategory = 'ROOM_TEMPERATURE';
        component.selectCategory(productCategory);

        expect(mockReceivingService.queryEnterShippingInformation).toHaveBeenCalledWith({
            productCategory,
            employeeId: 'testEmployeeId',
            locationCode: 'testFacility'
        });
        expect(component.form.get('temperatureProductCategory').value).toBe(productCategory);
    });

    it('should get location code from cookie', () => {
        expect(component.locationCodeComputed()).toBe('testFacility');
        expect(mockCookieService.get).toHaveBeenCalledWith(Cookie.XFacility);
    });

    it('should get employee ID from auth state', () => {
        expect(component.employeeIdComputed()).toBe('testEmployeeId');
    });

    it('should update form validators based on shipping information', () => {
        const mockUpdateFormValidators =  jest.spyOn(component, 'updateFormValidators');
        component.selectCategory('ROOM_TEMPERATURE');
        expect(mockUpdateFormValidators).toHaveBeenCalled()
    });

    it('should call updateFormValidationForTransitTime and updateFormValidationForTemperature', () => {
        const mockUpdateFormValidationForTransitTime =  jest.spyOn(component, 'updateFormValidationForTransitTime');
        const mockUpdateFormValidationForTemperature = jest.spyOn(component, 'updateFormValidationForTemperature');
        component.updateFormValidators({} as ShippingInformationDTO);
        expect(mockUpdateFormValidationForTransitTime).toHaveBeenCalled();
        expect(mockUpdateFormValidationForTemperature).toHaveBeenCalled();
    });

    it('should call updateValidators for temperature when useTemperature is true', fakeAsync(() => {
        const mockUpdateValidators = jest.fn();
        const mockTemperatureComponent = { updateValidators: mockUpdateValidators };
        
        // Mock the viewChild signals to return the mock components
        Object.defineProperty(component, 'temperatureFormComponent', {
            value: () => mockTemperatureComponent,
            writable: true
        });
        component.updateFormValidationForTemperature(true);
        tick();
        expect(mockUpdateValidators).toHaveBeenCalled();
    }));

    it('should update available time zones when shipping information changes', fakeAsync(() => {
        component.selectCategory('ROOM_TEMPERATURE');
        tick();
        expect(component.availableTimeZonesSignal()).toEqual([
            { descriptionKey: 'UTC' },
            { descriptionKey: 'GMT' },
        ]);
    }));

    describe('selectCategoryFromLookup', () => {
        it('should call selectCategory with lookup option value', () => {
            const mockLookup = { id: 1, descriptionKey: 'Test', optionValue: 'TEST_VALUE', type: '', active: true };
            jest.spyOn(component, 'selectCategory');
            
            component.selectCategoryFromLookup(mockLookup);
            
            expect(component.selectCategory).toHaveBeenCalledWith('TEST_VALUE');
        });
    });

    describe('cancel', () => {
        it('should reset all forms', () => {
            jest.spyOn(component.form, 'reset');
            const mockTransitTimeReset = jest.fn();
            const mockTemperatureReset = jest.fn();
            
            const mockTransitTimeComponent = { reset: mockTransitTimeReset };
            const mockTemperatureComponent = { reset: mockTemperatureReset };
            
            // Mock the viewChild signals to return the mock components
            Object.defineProperty(component, 'transitTimeFormComponent', {
                value: () => mockTransitTimeComponent,
                writable: true
            });
            Object.defineProperty(component, 'temperatureFormComponent', {
                value: () => mockTemperatureComponent,
                writable: true
            });
            
            component.cancel();
            expect(component.form.reset).toHaveBeenCalled();
            expect(mockTransitTimeReset).toHaveBeenCalled();
            expect(mockTemperatureReset).toHaveBeenCalled();
        });
    });

    describe('createImport', () => {
        it('should call triggerCreateImport', () => {
            jest.spyOn(component, 'triggerCreateImport').mockReturnValue(of());
            component.createImport();
            expect(component.triggerCreateImport).toHaveBeenCalled();
        });
    });

    describe('isFormValid', () => {
        it('should return false when main form is invalid', () => {
            component.form.setErrors({ invalid: true });
            expect(component.isFormValid()).toBeFalsy();
        });
    });

    describe('onTransitTimeChange', () => {
        it('should trigger transit time validation', () => {
            const mockTransitTime = {
                startDate: DateTime.fromISO('2023-12-25'),
                startTime: '09:00',
                startZone: 'UTC',
                endDate: DateTime.fromISO('2023-12-26'),
                endTime: '10:00',
                endZone: 'UTC'
            };
            component.form.controls.temperatureProductCategory.setValue('ROOM_TEMPERATURE');
            jest.spyOn(component, 'triggerValidateTransitTime').mockReturnValue(of({}));
            component.onTransitTimeChange(mockTransitTime);
            expect(component.triggerValidateTransitTime).toHaveBeenCalledWith('ROOM_TEMPERATURE', mockTransitTime);
        });
    });

    describe('onTemperatureChange', () => {
        it('should trigger temperature validation', () => {
            const mockData = { temperatureProductCategory: 'ROOM_TEMPERATURE', temperature: 25 };
            component.form.controls.temperatureProductCategory.setValue('ROOM_TEMPERATURE');
            jest.spyOn(component, 'triggerValidateTemperature').mockReturnValue(of({}));
            component.onTemperatureChange(mockData);
            expect(component.triggerValidateTemperature).toHaveBeenCalledWith('ROOM_TEMPERATURE', 25);
        });
    });

    describe('triggerCreateImport', () => {
        beforeEach(() => {
            const mockTransitTimeComponent = {
                formGroup: () => ({
                    controls: {
                        startDate: { value: DateTime.fromISO('2023-12-25') },
                        startTime: { value: '09:00' },
                        startZone: { value: 'UTC' },
                        endDate: { value: DateTime.fromISO('2023-12-26') },
                        endTime: { value: '10:00' },
                        endZone: { value: 'UTC' }
                    }
                })
            };
            const mockTemperatureComponent = {
                formGroup: () => ({
                    controls: {
                        temperature: { value: 25 },
                        thermometerId: { value: 'THERM001' }
                    }
                })
            };
           // Mock the viewChild signals
           Object.defineProperty(component, 'transitTimeFormComponent', {
            value: () => mockTransitTimeComponent,
            writable: true
            });
            Object.defineProperty(component, 'temperatureFormComponent', {
                value: () => mockTemperatureComponent,
                writable: true
            });
            component.form.controls.temperatureProductCategory.setValue('ROOM_TEMPERATURE');
            component.form.controls.comments.setValue('Test comment');
        });

        it('should create import with correct parameters', () => {
            mockReceivingService.createImport.mockReturnValue(of({
                data: {
                    createImport: {
                        notifications: [{ type: 'SUCCESS', message: 'Import created' }],
                        _links: { next: '/next-page' }
                    }
                }
            }));
            
            component.triggerCreateImport().subscribe();
            
            expect(mockReceivingService.createImport).toHaveBeenCalledWith({
                temperatureCategory: 'ROOM_TEMPERATURE',
                transitStartDateTime: expect.any(String),
                transitStartTimeZone: 'UTC',
                transitEndDateTime: expect.any(String),
                transitEndTimeZone: 'UTC',
                temperature: 25,
                thermometerCode: 'THERM001',
                locationCode: 'testFacility',
                comments: 'Test comment',
                employeeId: 'testEmployeeId'
            });
        });

        it('should navigate on successful import creation', () => {
            mockReceivingService.createImport.mockReturnValue(of({
                data: {
                    createImport: {
                        notifications: [{ type: 'SUCCESS', message: 'Import created' }],
                        _links: { next: '/next-page' }
                    }
                }
            }));

            component.triggerCreateImport().subscribe();
            mockRouter.navigateByUrl.mockReturnValue(Promise.resolve(true))
            expect(mockReceivingService.createImport).toHaveBeenCalled();
            component.triggerCreateImport().subscribe(() => {
                expect(mockRouter.navigateByUrl).toHaveBeenCalledWith('/next-page');
            });
        });
    });

    describe('buildLuxonDateTimeWithParsedTimeField', () => {
        it('should correctly build DateTime from date and time string', () => {
            const date = DateTime.fromISO('2023-12-25');
            const timeStr = '14:30';

            const result = component.buildLuxonDateTimeWithParsedTimeField(date, timeStr);

            expect(result.year).toBe(2023);
            expect(result.month).toBe(12);
            expect(result.day).toBe(25);
            expect(result.hour).toBe(14);
            expect(result.minute).toBe(30);
        });

        it('should return null when date is null', () => {
            const result = component.buildLuxonDateTimeWithParsedTimeField(null, '14:30');
            expect(result).toBeNull();
        });

        it('should return null when time string is null', () => {
            const date = DateTime.fromISO('2023-12-25');
            const result = component.buildLuxonDateTimeWithParsedTimeField(date, null);
            expect(result).toBeNull();
        });
    });

    describe('triggerElementBlur', () => {
        it('should call blur on the target element', () => {
            const mockElement = { blur: jest.fn() };
            const mockEvent = { target: mockElement };
            component.triggerElementBlur(mockEvent as unknown as Event);
            expect(mockElement.blur).toHaveBeenCalled();
        });
    });

    describe('error handling', () => {
        it('should handle Apollo errors during shipping information fetch', () => {
            const apolloError = new ApolloError({
                graphQLErrors: [{ message: 'GraphQL Error' }],
                networkError: null
            });

            mockReceivingService.queryEnterShippingInformation.mockReturnValueOnce(throwError(() => apolloError));
            component.selectCategory('ROOM_TEMPERATURE');
            expect(mockToastr.error).toHaveBeenCalledWith('GraphQL Error');
        });

        it('should handle notifications from shipping information response', () => {
            const mockShippingInfo = {
                data: {
                    enterShippingInformation: {
                        data: {
                            productCategory : "ROOM_TEMPERATURE",
                            temperatureUnit : "celsius",
                            displayTransitInformation : true,
                            displayTemperature : true,
                            transitTimeZoneList : [ {
                            id : 4,
                            type : "TRANSIT_TIME_ZONE",
                            optionValue : "America/New_York",
                            descriptionKey : "ET",
                            orderNumber : 1,
                            active : true
                            }],
                            visualInspectionList : [],
                            defaultTimeZone : "America/New_York"
                        },
                        _links: null,
                        notifications: [{ type: 'SUCCESS', message: 'Test success' }]
                    }
                }
            } as ApolloQueryResult<{ enterShippingInformation: UseCaseResponseDTO<ShippingInformationDTO> }>
            mockReceivingService.queryEnterShippingInformation.mockReturnValueOnce(of(mockShippingInfo));

            component.selectCategory('ROOM_TEMPERATURE');
            expect(mockToastr.show).toHaveBeenCalledWith('Test success', null, {}, 'success');
        });
    });

    describe('form validation', () => {
        it('should update form validators based on shipping information', () => {
            jest.spyOn(component, 'updateFormValidationForTransitTime');
            jest.spyOn(component, 'updateFormValidationForTemperature');
            component.selectCategory('ROOM_TEMPERATURE');
            expect(component.updateFormValidationForTransitTime).toHaveBeenCalledWith(true);
            expect(component.updateFormValidationForTemperature).toHaveBeenCalledWith(true);
        });
    });
});