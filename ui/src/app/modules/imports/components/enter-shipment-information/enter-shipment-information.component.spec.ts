import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EnterShipmentInformationComponent } from './enter-shipment-information.component';
import { Router } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProcessHeaderService, ToastrImplService } from '@shared';
import { ImportsService } from '../../service/imports.service';
import { CookieService } from 'ngx-cookie-service';
import { of, throwError } from 'rxjs';
import { ApolloError, ApolloQueryResult } from '@apollo/client';
import { Cookie } from '../../../../shared/types/cookie.enum';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ShippingInformationDTO } from '../../graphql/query-definitions/enter-shipping-information.graphql';
import { UseCaseResponseDTO } from '../../../../shared/models/use-case-response.dto';
import { MatIconTestingModule } from '@angular/material/icon/testing';
import { MockStore, provideMockStore } from '@ngrx/store/testing';

describe('EnterShipmentInformationComponent', () => {
  let component: EnterShipmentInformationComponent;
  let fixture: ComponentFixture<EnterShipmentInformationComponent>;
  let mockRouter: jest.Mocked<Router>;
  let mockToastr: jest.Mocked<ToastrImplService>;
  let mockHeader: jest.Mocked<ProcessHeaderService>;
  let mockStore: MockStore;
  let mockImportsService: jest.Mocked<ImportsService>;
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
      success: jest.fn(),
      error: jest.fn(),
      warning: jest.fn()
    } as any;
    mockHeader = {} as any;
    mockImportsService = {
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

    await TestBed.configureTestingModule({
      imports: [
        EnterShipmentInformationComponent,
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
        { provide: ToastrImplService, useValue: mockToastr },
        { provide: ProcessHeaderService, useValue: mockHeader },
        { provide: ImportsService, useValue: mockImportsService },
        { provide: CookieService, useValue: mockCookieService }
      ]
    }).compileComponents();

    mockStore = TestBed.inject(MockStore);
    fixture = TestBed.createComponent(EnterShipmentInformationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with empty values', () => {
    expect(component.form.get('productCategory').value).toBeNull();
    expect(component.form.get('transitTime.startDate').value).toBeNull();
    expect(component.form.get('temperature').value).toBeNull();
    expect(component.form.get('thermometerId').value).toBeNull();
    expect(component.form.get('comments').value).toBeNull();
  });

  it('should fetch lookups on init', () => {
    component.ngOnInit();
    expect(mockImportsService.findAllLookupsByType).toHaveBeenCalledWith('TEMPERATURE_PRODUCT_CATEGORY');
    expect(component.productCategoriesSignal()).toEqual(mockLookups);
  });

  it('should handle lookup fetching error', () => {
    const error = new ApolloError({ graphQLErrors: [{ message: 'Test error' }] });
    mockImportsService.findAllLookupsByType.mockReturnValueOnce(throwError(() => error));

    component.ngOnInit();

    expect(mockToastr.error).toHaveBeenCalled();
  });

  it('should fetch shipping information when selecting category', () => {
    const productCategory = 'CAT1';
    component.selectCategory(productCategory);

    expect(mockImportsService.queryEnterShippingInformation).toHaveBeenCalledWith({
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
    expect(component.form.get('transitTime.endDate').hasValidator(Validators.required)).toBeTruthy();

    // Temperature validators should be required
    expect(component.form.get('temperature').hasValidator(Validators.required)).toBeTruthy();
    expect(component.form.get('thermometerId').hasValidator(Validators.required)).toBeTruthy();
  });

  it('should clear form validators when display flags are false', () => {
    mockImportsService.queryEnterShippingInformation.mockReturnValueOnce(of({
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
    expect(component.form.get('transitTime.endDate').hasValidator(Validators.required)).toBeFalsy();

    // Temperature validators should be cleared
    expect(component.form.get('temperature').hasValidator(Validators.required)).toBeFalsy();
    expect(component.form.get('thermometerId').hasValidator(Validators.required)).toBeFalsy();
  });

  it('should reset form on cancel', () => {
    component.form.patchValue({
      productCategory: 'CAT1',
      temperature: 20,
      thermometerId: '123',
      comments: 'test'
    });

    component.cancel();

    expect(component.form.get('productCategory').value).toBeNull();
    expect(component.form.get('temperature').value).toBeNull();
    expect(component.form.get('thermometerId').value).toBeNull();
    expect(component.form.get('comments').value).toBeNull();
  });

  it('should handle shipping information fetching error', () => {
    const error = new ApolloError({ graphQLErrors: [{ message: 'Test error' }] });
    mockImportsService.queryEnterShippingInformation.mockReturnValueOnce(throwError(() => error));

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
});
