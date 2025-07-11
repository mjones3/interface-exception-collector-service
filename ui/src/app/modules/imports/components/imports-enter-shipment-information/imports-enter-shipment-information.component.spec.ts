import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { ImportsEnterShipmentInformationComponent } from './imports-enter-shipment-information.component';
import { Router } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProcessHeaderService } from '@shared';
import { ReceivingService } from '../../service/receiving.service';
import { CookieService } from 'ngx-cookie-service';
import { Observable, of, throwError } from 'rxjs';
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
import { By } from '@angular/platform-browser';
import { DeviceIdValidator } from 'app/shared/forms/device-id.validator';

xdescribe('ImportsEnterShipmentInformationComponent', () => {
    let component: ImportsEnterShipmentInformationComponent;
    let fixture: ComponentFixture<ImportsEnterShipmentInformationComponent>;
    let mockRouter: jest.Mocked<Router>;
    let mockToastr: jest.Mocked<ToastrService>;
    let mockHeader: jest.Mocked<ProcessHeaderService>;
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
        displayTransitInformation: true,
        displayTemperature: true
    };

    beforeEach(async () => {
        mockRouter = { navigate: jest.fn() } as any;
        mockToastr = {
            show: jest.fn(),
            success: jest.fn(),
            error: jest.fn(),
            warning: jest.fn()
        } as any;
        mockHeader = {} as any;
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

        // Mock DeviceValidator
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
                { provide: ProcessHeaderService, useValue: mockHeader },
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
        expect(component.form.get('transitTime.startDate').value).toBeNull();
        expect(component.form.get('transitTime.startTime').value).toBeNull();
        expect(component.form.get('transitTime.startZone').value).toBeNull();
        expect(component.form.get('transitTime.endDate').value).toBeNull();
        expect(component.form.get('transitTime.endTime').value).toBeNull();
        expect(component.form.get('transitTime.endZone').value).toBeNull();
        expect(component.form.get('temperature.temperature').value).toBeNull();
        expect(component.form.get('temperature.thermometerId').value).toBeNull();
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

    it('should update form validators based on shipping information', () => {
        component.selectCategory('ROOM_TEMPERATURE');

        // Transit time validators should be required
        expect(component.form.get('transitTime.startDate').hasValidator(Validators.required)).toBeTruthy();
        expect(component.form.get('transitTime.startTime').hasValidator(Validators.required)).toBeTruthy();
        expect(component.form.get('transitTime.startZone').hasValidator(Validators.required)).toBeTruthy();
        expect(component.form.get('transitTime.endDate').hasValidator(Validators.required)).toBeTruthy();
        expect(component.form.get('transitTime.endTime').hasValidator(Validators.required)).toBeTruthy();
        expect(component.form.get('transitTime.endZone').hasValidator(Validators.required)).toBeTruthy();

        // Temperature validators should be required
        expect(component.form.get('temperature.temperature').hasValidator(Validators.required)).toBeTruthy();
        expect(component.form.get('temperature.thermometerId').hasValidator(Validators.required)).toBeTruthy();
    });

    it('should clear form validators when display flags are false', () => {
        mockReceivingService.queryEnterShippingInformation.mockReturnValueOnce(of({
            data: {
                enterShippingInformation: {
                    data: {
                        ...mockShippingInfo,
                        displayTransitInformation: false,
                        displayTemperature: false
                    } as unknown as ShippingInformationDTO,
                    notifications: []
                }
            }
        } as unknown as ApolloQueryResult<{ enterShippingInformation: UseCaseResponseDTO<ShippingInformationDTO> }>));

        component.selectCategory('ROOM_TEMPERATURE');

        // Transit time validators should be cleared
        expect(component.form.get('transitTime.startDate').hasValidator(Validators.required)).toBeFalsy();
        expect(component.form.get('transitTime.startTime').hasValidator(Validators.required)).toBeFalsy();
        expect(component.form.get('transitTime.startZone').hasValidator(Validators.required)).toBeFalsy();
        expect(component.form.get('transitTime.endDate').hasValidator(Validators.required)).toBeFalsy();
        expect(component.form.get('transitTime.endTime').hasValidator(Validators.required)).toBeFalsy();
        expect(component.form.get('transitTime.endZone').hasValidator(Validators.required)).toBeFalsy();

        // Temperature validators should be cleared
        expect(component.form.get('temperature.temperature').hasValidator(Validators.required)).toBeFalsy();
        expect(component.form.get('temperature.thermometerId').hasValidator(Validators.required)).toBeFalsy();
    });

    it('should get location code from cookie', () => {
        expect(component.locationCodeComputed()).toBe('testFacility');
        expect(mockCookieService.get).toHaveBeenCalledWith(Cookie.XFacility);
    });

    it('should get employee ID from auth state', () => {
        expect(component.employeeIdComputed()).toBe('testEmployeeId');
    });

    it('should update available time zones when shipping information changes', fakeAsync(() => {
        component.selectCategory('ROOM_TEMPERATURE');
        tick(); // Wait for async operations
        expect(component.availableTimeZonesSignal()).toEqual([
            { descriptionKey: 'UTC' },
            { descriptionKey: 'GMT' },
        ]);
    }));

    xdescribe('thermometer validation effect', () => {
        it('should disable temperature input when thermometer ID is invalid', () => {
            // First set it to valid state
            component.form.get('temperature.thermometerId').setValue('VALID_ID');
            component.form.get('temperature.thermometerId').setErrors(null);

            // Then make it invalid
            component.form.get('temperature.thermometerId').setValue('');
            component.form.get('temperature.thermometerId').setErrors({ required: true });

            // Temperature input should be disabled
            expect(component.form.get('temperature.temperature').disabled).toBeTruthy();
        });

        it('should disable temperature input when thermometer ID is cleared', () => {
            // First set it to valid state
            component.form.get('temperature.thermometerId').setValue('VALID_ID');
            component.form.get('temperature.thermometerId').setErrors(null);

            // Then clear the value
            component.form.get('temperature.thermometerId').setValue(null);

            // Temperature input should be disabled
            expect(component.form.get('temperature.temperature').disabled).toBeTruthy();
        });
    });

    xdescribe('form validation with temperature requirements', () => {
        beforeEach(() => {
            // Setup shipping info with temperature requirements
            mockReceivingService.queryEnterShippingInformation.mockReturnValueOnce(of({
                data: {
                    enterShippingInformation: {
                        data: {
                            ...mockShippingInfo,
                            displayTemperature: true
                        } as unknown as ShippingInformationDTO,
                        notifications: []
                    }
                }
            } as unknown as ApolloQueryResult<{ enterShippingInformation: UseCaseResponseDTO<ShippingInformationDTO> }>));

            component.selectCategory('ROOM_TEMPERATURE');
        });

        it('should add async validators for temperature when temperature is required', () => {
            const thermometerControl = component.form.get('temperature.thermometerId');
            const temperatureControl = component.form.get('temperature.temperature');

            expect(thermometerControl.hasValidator(Validators.required)).toBeTruthy();
            expect(thermometerControl.hasAsyncValidator(DeviceIdValidator.asyncValidatorUsing(mockToastr, mockReceivingService, 'testFacility'))).toBeTruthy();
            expect(temperatureControl.hasValidator(Validators.required)).toBeTruthy();
        });

        it('should clear async validators when temperature is not required', () => {
            // Change shipping info to not require temperature
            mockReceivingService.queryEnterShippingInformation.mockReturnValueOnce(of({
                data: {
                    enterShippingInformation: {
                        data: {
                            ...mockShippingInfo,
                            displayTemperature: false
                        } as unknown as ShippingInformationDTO,
                        notifications: []
                    }
                }
            } as unknown as ApolloQueryResult<{ enterShippingInformation: UseCaseResponseDTO<ShippingInformationDTO> }>));

            component.selectCategory('ROOM_TEMPERATURE');

            const thermometerControl = component.form.get('temperature.thermometerId');
            const temperatureControl = component.form.get('temperature.temperature');

            expect(thermometerControl.hasValidator(Validators.required)).toBeFalsy();
            expect(thermometerControl.asyncValidator).toBeNull();
            expect(temperatureControl.hasValidator(Validators.required)).toBeFalsy();
            expect(thermometerControl.disabled).toBeTruthy();
            expect(temperatureControl.disabled).toBeTruthy();
        });

        it('should properly handle form state when updating validators', () => {
            const thermometerControl = component.form.get('temperature.thermometerId');
            const temperatureControl = component.form.get('temperature.temperature');

            // Set initial values
            thermometerControl.setValue('VALID_ID');
            thermometerControl.setErrors(null);
            temperatureControl.setValue(20);
            fixture.detectChanges();

            // Simulate changing to a category that doesn't require temperature
            mockReceivingService.queryEnterShippingInformation.mockReturnValueOnce(of({
                data: {
                    enterShippingInformation: {
                        data: {
                            ...mockShippingInfo,
                            displayTemperature: false
                        } as unknown as ShippingInformationDTO,
                        notifications: []
                    }
                }
            } as unknown as ApolloQueryResult<{ enterShippingInformation: UseCaseResponseDTO<ShippingInformationDTO> }>));

            component.selectCategory('ROOM_TEMPERATURE');

            // Form should be reset and temperature should be disabled
            expect(thermometerControl.value).toBeNull();
            expect(temperatureControl.value).toBeNull();
            expect(thermometerControl.disabled).toBeTruthy();
            expect(temperatureControl.disabled).toBeTruthy();
        });
    });

    xdescribe('temperature field validation', () => {
        beforeEach(() => {
            // Enable temperature input by setting valid thermometer ID
            component.form.get('temperature.thermometerId').setValue('VALID_ID');
            component.form.get('temperature.thermometerId').setErrors(null);
            fixture.detectChanges();
        });

        it('should enforce minimum temperature limit', fakeAsync(() => {
            // First enable temperature validation by simulating category selection
            mockReceivingService.queryEnterShippingInformation.mockReturnValueOnce(of({
                data: {
                    enterShippingInformation: {
                        data: {
                            ...mockShippingInfo,
                            displayTemperature: true
                        },
                        notifications: []
                    }
                }
            }) as unknown as Observable<ApolloQueryResult<{ enterShippingInformation: UseCaseResponseDTO<ShippingInformationDTO> }>>);
            component.selectCategory('ROOM_TEMPERATURE');
            tick();

            // Enable temperature input by setting valid thermometer ID
            const thermometerControl = component.form.get('temperature.thermometerId');
            thermometerControl.setValue('VALID_ID');
            thermometerControl.setErrors(null);
            tick();

            const temperatureControl = component.form.get('temperature.temperature');
            temperatureControl.enable(); // Explicitly enable the control

            // Test minimum value validation
            temperatureControl.setValue(-274); // Below minimum of -273
            tick();

            expect(temperatureControl.errors?.['min']).toBeTruthy();
            expect(temperatureControl.valid).toBeFalsy();

            temperatureControl.setValue(-273); // At minimum
            tick();
            expect(temperatureControl.errors?.['min']).toBeFalsy();
            expect(temperatureControl.valid).toBeTruthy();
        }));

        it('should enforce maximum temperature limit', fakeAsync(() => {
            // First enable temperature validation by simulating category selection
            mockReceivingService.queryEnterShippingInformation.mockReturnValueOnce(of({
                data: {
                    enterShippingInformation: {
                        data: {
                            ...mockShippingInfo,
                            displayTemperature: true
                        },
                        notifications: []
                    }
                }
            }) as unknown as Observable<ApolloQueryResult<{ enterShippingInformation: UseCaseResponseDTO<ShippingInformationDTO> }>>);
            component.selectCategory('ROOM_TEMPERATURE');
            tick();

            // Enable temperature input by setting valid thermometer ID
            const thermometerControl = component.form.get('temperature.thermometerId');
            thermometerControl.setValue('VALID_ID');
            thermometerControl.setErrors(null);
            tick();

            const temperatureControl = component.form.get('temperature.temperature');
            temperatureControl.enable(); // Explicitly enable the control

            // Test maximum value validation
            temperatureControl.setValue(100); // Above maximum of 99
            tick();

            expect(temperatureControl.errors?.['max']).toBeTruthy();
            expect(temperatureControl.valid).toBeFalsy();

            temperatureControl.setValue(99); // At maximum
            tick();
            expect(temperatureControl.errors?.['max']).toBeFalsy();
            expect(temperatureControl.valid).toBeTruthy();
        }));

        it('should not trigger temperature validation for empty values', (done) => {
            const temperatureControl = component.form.get('temperature.temperature');
            mockReceivingService.validateTemperature = jest.fn();

            temperatureControl.setValue(null);

            // Wait for debounce
            setTimeout(() => {
                expect(mockReceivingService.validateTemperature).not.toHaveBeenCalled();
                done();
            }, 600);
        });
    });

    xdescribe('buildLuxonDateTimeWithParsedTimeField', () => {
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

        it('should handle different time formats', () => {
            const date = DateTime.fromISO('2023-12-25');
            const timeStr = '09:05';

            const result = component.buildLuxonDateTimeWithParsedTimeField(date, timeStr);

            expect(result.hour).toBe(9);
            expect(result.minute).toBe(5);
        });
    });

    xdescribe('triggerBlur', () => {
        it('should call blur on the target element', () => {
            const mockElement = { blur: jest.fn() };
            const mockEvent = { target: mockElement };

            component.triggerElementBlur(mockEvent as unknown as Event);

            expect(mockElement.blur).toHaveBeenCalled();
        });
    });

    xdescribe('transit time validation', () => {
        beforeEach(() => {
            component.selectCategory('ROOM_TEMPERATURE');
            fixture.detectChanges();
        });

        it('should clear transit time signals when form is invalid', fakeAsync(() => {
            // First set valid values
            const transitTimeGroup = component.form.controls.transitTime;
            transitTimeGroup.patchValue({
                startDate: DateTime.fromISO('2023-12-25'),
                startTime: '09:00',
                startZone: 'UTC',
                endDate: DateTime.fromISO('2023-12-26'),
                endTime: '10:00',
                endZone: 'UTC'
            });
            transitTimeGroup.markAsTouched();
            tick();

            // Then make form invalid
            transitTimeGroup.patchValue({ startTime: null });
            tick();

            expect(component.transitTimeHumanReadableSignal()).toBeNull();
            expect(component.transitTimeQuarantineSignal()).toBeNull();
        }));

        xdescribe('error handling and notifications', () => {
            it('should handle notifications from shipping information response', () => {
                // Mock response with notifications
                mockReceivingService.queryEnterShippingInformation.mockReturnValueOnce(of({
                    data: {
                        enterShippingInformation: {
                            data: mockShippingInfo,
                            notifications: [
                                { type: 'WARN', message: 'Test warning' },
                                { type: 'SUCCESS', message: 'Test success' }
                            ]
                        }
                    }
                } as unknown as ApolloQueryResult<{ enterShippingInformation: UseCaseResponseDTO<ShippingInformationDTO> }>));

                component.selectCategory('ROOM_TEMPERATURE');

                expect(mockToastr.show).toHaveBeenNthCalledWith(1, 'Test warning', null, {}, 'error');
                expect(mockToastr.show).toHaveBeenNthCalledWith(2, 'Test success', null, {}, 'success');
            });
        });

        it('should handle Apollo errors during shipping information fetch', () => {
            const apolloError = new ApolloError({
                graphQLErrors: [{ message: 'GraphQL Error' }],
                networkError: null
            });

            mockReceivingService.queryEnterShippingInformation.mockReturnValueOnce(throwError(() => apolloError));

            component.selectCategory('ROOM_TEMPERATURE');

            expect(mockToastr.error).toHaveBeenCalledWith('GraphQL Error');
            expect(component.form.get('temperatureProductCategory').value).toBeNull();
        });
    });

    xdescribe('Continue button', () => {
        it('should be disabled when form is invalid', () => {
            // Set form as invalid
            component.form.setErrors({ 'invalid': true });
            fixture.detectChanges();

            // Find the continue button
            const continueButton = fixture.debugElement.query(
                By.css('#importsEnterShipmentInformationContinueActionButton')
            );

            // Check if button is disabled
            expect(continueButton.nativeElement.getAttribute('disabled')).toBe('true');
        });

        it('should be enabled when form is valid', () => {
            // Set form as valid
            component.form.clearValidators();
            component.form.controls.temperatureProductCategory.setValue('ROOM_TEMPERATURE');
            fixture.detectChanges();

            // Find the continue button
            const continueButton = fixture.debugElement.query(
                By.css('#importsEnterShipmentInformationContinueActionButton')
            );

            // Check if button is enabled
            expect(continueButton.nativeElement.getAttribute('disabled')).toBeFalsy();
        });
    });

});
