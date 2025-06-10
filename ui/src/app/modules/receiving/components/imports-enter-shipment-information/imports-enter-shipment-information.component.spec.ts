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
    expect(component.productCategoriesSignal()).toEqual(mockLookups);
  });

  it('should handle lookup fetching error', () => {
    const error = new ApolloError({ graphQLErrors: [{ message: 'Test error' }] });
    mockReceivingService.findAllLookupsByType.mockReturnValueOnce(throwError(() => error));

    component.ngOnInit();

    expect(mockToastr.error).toHaveBeenCalled();
  });

  it('should fetch shipping information when selecting category', () => {
    const productCategory = 'CAT1';
    component.selectCategory(productCategory);

    expect(mockReceivingService.queryEnterShippingInformation).toHaveBeenCalledWith({
      productCategory,
      employeeId: 'testEmployeeId',
      locationCode: 'testFacility'
    });
    expect(component.form.get('productCategory').value).toBe(productCategory);
  });

  it('should update form validators based on shipping information', () => {
    component.selectCategory('CAT1');

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

    component.selectCategory('CAT1');

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

  it('should handle shipping information fetching error', () => {
    const error = new ApolloError({ graphQLErrors: [{ message: 'Test error' }] });
    mockReceivingService.queryEnterShippingInformation.mockReturnValueOnce(throwError(() => error));

    component.selectCategory('CAT1');

    expect(mockToastr.error).toHaveBeenCalled();
  });

  it('should get location code from cookie', () => {
    expect(component.locationCodeComputed()).toBe('testFacility');
    expect(mockCookieService.get).toHaveBeenCalledWith(Cookie.XFacility);
  });

  it('should get employee ID from auth state', () => {
    expect(component.employeeIdComputed()).toBe('testEmployeeId');
  });

  it('should update available time zones when shipping information changes', () => {
    component.selectCategory('CAT1');
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

      component.selectCategory('CAT1');
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

      component.selectCategory('CAT2');

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

      component.selectCategory('CAT2');

      // Values should be preserved even though validators are removed
      expect(thermometerControl.value).toBeNull();
      expect(temperatureControl.value).toBeNull();
      expect(thermometerControl.enabled).toBeTruthy();
      expect(temperatureControl.disabled).toBeTruthy(); // Should still be disabled due to the effect
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

      component.selectCategory('CAT1');

      expect(mockToastr.show).toHaveBeenNthCalledWith(1, 'Test warning', null, {}, 'error');
      expect(mockToastr.show).toHaveBeenNthCalledWith(2, 'Test success', null, {}, 'success');
    });

    it('should handle Apollo errors during shipping information fetch', () => {
      const apolloError = new ApolloError({
        graphQLErrors: [{ message: 'GraphQL Error' }],
        networkError: null
      });

      mockReceivingService.queryEnterShippingInformation.mockReturnValueOnce(throwError(() => apolloError));

      component.selectCategory('CAT1');

      expect(mockToastr.error).toHaveBeenCalledWith('GraphQL Error');
      expect(component.form.get('productCategory').value).toBeNull();
    });

    it('should maintain form state when error occurs during shipping information fetch', () => {
      // Set initial form state
      component.form.patchValue({
        productCategory: 'OLD_CATEGORY',
        comments: 'Test comment'
      });

      // Simulate error
      mockReceivingService.queryEnterShippingInformation.mockReturnValueOnce(throwError(() => new ApolloError({ graphQLErrors: [{ message: 'Error' }] })));

      component.selectCategory('NEW_CATEGORY');

      // Form should maintain old values
      expect(component.form.get('productCategory').value).toBe('OLD_CATEGORY');
      expect(component.form.get('comments').value).toBe('Test comment');
    });
  });
});
