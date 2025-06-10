import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ImportsEnterShipmentInformationComponent } from './imports-enter-shipment-information.component';
import { Router } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProcessHeaderService } from '@shared';
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
import { DeviceIdValidator } from '../../validators/deviceIdValidator';
import { ToastrService } from 'ngx-toastr';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';

describe('ImportsEnterShipmentInformationComponent', () => {
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
      }))
    } as any;
    mockCookieService = {
      get: jest.fn().mockReturnValue('testFacility')
    } as any;

    // Mock DeviceValidator
    jest.spyOn(DeviceIdValidator, 'using').mockReturnValue(() => of(null));

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
    expect(component.form.get('productCategory').value).toBeNull();
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
    expect(component.form.get('productCategory').value).toBe(productCategory);
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

  it('should reset form on cancel', () => {
    component.form.patchValue({
      productCategory: 'CAT1',
      temperature: {
        temperature: 20,
        thermometerId: '123'
      },
      comments: 'test'
    });

    component.cancel();

    expect(component.form.get('productCategory').value).toBeNull();
    expect(component.form.get('temperature.temperature').value).toBeNull();
    expect(component.form.get('temperature.thermometerId').value).toBeNull();
    expect(component.form.get('comments').value).toBeNull();
  });

  it('should get location code from cookie', () => {
    expect(component.locationCodeComputed()).toBe('testFacility');
    expect(mockCookieService.get).toHaveBeenCalledWith(Cookie.XFacility);
  });

  it('should get employee ID from auth state', () => {
    expect(component.employeeIdComputed()).toBe('testEmployeeId');
  });

  it('should update available time zones when shipping information changes', () => {
    component.selectCategory('ROOM_TEMPERATURE');
    expect(component.availableTimeZonesSignal()).toEqual(['UTC', 'GMT']);
  });

  describe('thermometer validation effect', () => {
    it('should enable temperature input when thermometer ID is valid', () => {
      // Initially temperature input should be disabled
      expect(component.form.get('temperature.temperature').disabled).toBeTruthy();

      // Set a valid thermometer ID
      component.form.get('temperature.thermometerId').setValue('VALID_ID');
      // Simulate valid status
      component.form.get('temperature.thermometerId').setErrors(null);
      fixture.detectChanges();

      // Temperature input should now be enabled
      expect(component.form.get('temperature.temperature').disabled).toBeFalsy();
    });

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

  describe('form validation with temperature requirements', () => {
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
      expect(thermometerControl.hasAsyncValidator(DeviceIdValidator.using(mockToastr, mockReceivingService, 'testFacility'))).toBeTruthy();
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
    });

    it('should properly handle form state when updating validators', () => {
      const thermometerControl = component.form.get('temperature.thermometerId');
      const temperatureControl = component.form.get('temperature.temperature');

      // Set initial values
      thermometerControl.setValue('VALID_ID');
      temperatureControl.setValue(20);

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

      // Values should be preserved even though validators are removed
      expect(thermometerControl.value).toBeNull();
      expect(temperatureControl.value).toBeNull();
      expect(thermometerControl.enabled).toBeTruthy();
      expect(temperatureControl.disabled).toBeTruthy(); // Should still be disabled due to the effect
    });
  });

  describe('temperature field validation', () => {
    beforeEach(() => {
        // Enable temperature input by setting valid thermometer ID
        component.form.get('temperature.thermometerId').setValue('VALID_ID');
        component.form.get('temperature.thermometerId').setErrors(null);
        fixture.detectChanges();
    });

    it('should enforce minimum temperature limit', () => {
        const temperatureControl = component.form.get('temperature.temperature');
        temperatureControl.setValue(-274); // Below minimum of -273

        expect(temperatureControl.errors?.['min']).toBeTruthy();
        expect(temperatureControl.valid).toBeFalsy();

        temperatureControl.setValue(-273); // At minimum
        expect(temperatureControl.errors?.['min']).toBeFalsy();
        expect(temperatureControl.valid).toBeTruthy();
    });

    it('should enforce maximum temperature limit', () => {
        const temperatureControl = component.form.get('temperature.temperature');
        temperatureControl.setValue(100); // Above maximum of 99

        expect(temperatureControl.errors?.['max']).toBeTruthy();
        expect(temperatureControl.valid).toBeFalsy();

        temperatureControl.setValue(99); // At maximum
        expect(temperatureControl.errors?.['max']).toBeFalsy();
        expect(temperatureControl.valid).toBeTruthy();
    });

    it('should validate temperature through service and handle notifications', (done) => {
        const temperatureControl = component.form.get('temperature.temperature');
        const productCategoryControl = component.form.get('productCategory');

        // Mock service response with a caution notification
        mockReceivingService.validateTemperature = jest.fn().mockReturnValue(of({
            data: {
                validateTemperature: {
                    notifications: [
                        { type: 'CAUTION', message: 'Temperature outside normal range' },
                        { type: 'WARN', message: 'Warning message' }
                    ]
                }
            }
        }));

        // Set temperature value to trigger validation
        temperatureControl.setValue(15);
        productCategoryControl.setValue('ROOM_TEMPERATURE');

        // Wait for debounce
        setTimeout(() => {
            expect(mockReceivingService.validateTemperature).toHaveBeenCalledWith({
                temperature: 15,
                temperatureCategory: 'ROOM_TEMPERATURE'
            });

            // Warning notification should be shown
            expect(mockToastr.show).toHaveBeenCalledWith('Warning message', null, {}, 'error');

            // Caution notification should be stored
            expect(component.temperatureQuarantineNotificationSignal()).toEqual({
                type: 'CAUTION',
                message: 'Temperature outside normal range'
            });

            done();
        }, 600); // Longer than the 400ms debounce time
    });

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

  describe('error handling and notifications', () => {
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

    it('should handle Apollo errors during shipping information fetch', () => {
      const apolloError = new ApolloError({
        graphQLErrors: [{ message: 'GraphQL Error' }],
        networkError: null
      });

      mockReceivingService.queryEnterShippingInformation.mockReturnValueOnce(throwError(() => apolloError));

      component.selectCategory('ROOM_TEMPERATURE');

      expect(mockToastr.error).toHaveBeenCalledWith('GraphQL Error');
      expect(component.form.get('productCategory').value).toBeNull();
    });

    it('should maintain form state when error occurs during shipping information fetch', () => {
      // Set initial form state
      component.form.patchValue({
        productCategory: 'ROOM_TEMPERATURE',
        comments: 'Test comment'
      });

      // Simulate error
      mockReceivingService.queryEnterShippingInformation.mockReturnValueOnce(throwError(() => new ApolloError({ graphQLErrors: [{ message: 'Error' }] })));

      component.selectCategory('REFRIGERATED');

      // Form should maintain old values
      expect(component.form.get('productCategory').value).toBe('ROOM_TEMPERATURE');
      expect(component.form.get('comments').value).toBe('Test comment');
    });
  });
});
