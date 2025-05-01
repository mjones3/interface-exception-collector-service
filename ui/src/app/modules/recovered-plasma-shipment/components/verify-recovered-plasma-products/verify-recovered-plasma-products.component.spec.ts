import { ComponentFixture, TestBed } from '@angular/core/testing';
import { VerifyRecoveredPlasmaProductsComponent } from './verify-recovered-plasma-products.component';
import { MockStore, provideMockStore } from '@ngrx/store/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { of } from 'rxjs';
import { ToastrModule } from 'ngx-toastr';
import { ApolloTestingModule } from 'apollo-angular/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ScanUnitNumberProductCodeComponent } from 'app/shared/components/scan-unit-number-product-code/scan-unit-number-product-code.component';
import { CookieService } from 'ngx-cookie-service';
import { ToastrImplService } from '@shared';
import { ProductIconsService } from 'app/shared/services/product-icon.service';
import { RecoveredPlasmaService } from '../../services/recovered-plasma.service';
import { CartonDTO } from '../../models/recovered-plasma.dto';
import { signal } from '@angular/core';

describe('VerifyRecoveredPlasmaProductsComponent', () => {
  let component: VerifyRecoveredPlasmaProductsComponent;
  let fixture: ComponentFixture<VerifyRecoveredPlasmaProductsComponent>;
  let mockStore: MockStore;
  let mockScanUnitNumberProductCode: jest.Mocked<ScanUnitNumberProductCodeComponent>;
  let mockRecoveredPlasmaService: jest.Mocked<RecoveredPlasmaService>;
  let mockRouter: jest.Mocked<Router>;
  let mockToastr: jest.Mocked<ToastrImplService>;
  let mockProductIconsService: jest.Mocked<ProductIconsService>;
  let mockCookieService: jest.Mocked<CookieService>;

  const mockCartonData: CartonDTO = {
    id: 12,
    cartonNumber: 'C123',
    maxNumberOfProducts: 10,
    totalProducts: 5,
    verifiedProducts: [
      { unitNumber: 'W2323232323232', productCode: 'E23232323', productType: 'TYPE1' },
      { unitNumber: 'W2323232323233', productCode: 'E23232323', productType: 'TYPE2' }
    ],
    canVerify: true
  };

  beforeEach(async () => {
    // Create mocks for all dependencies
    mockRouter = { navigate: jest.fn() } as Partial<Router> as jest.Mocked<Router>;
    
    mockRecoveredPlasmaService = {
      displayNotificationMessage: jest.fn()
    } as Partial<RecoveredPlasmaService> as jest.Mocked<RecoveredPlasmaService>;
    
    mockToastr = {
      success: jest.fn(),
      error: jest.fn()
    } as Partial<ToastrImplService> as jest.Mocked<ToastrImplService>;
    
    mockProductIconsService = {
      getIconByProductFamily: jest.fn().mockReturnValue('plasma-icon')
    } as Partial<ProductIconsService> as jest.Mocked<ProductIconsService>;
  
    
    mockCookieService = {
      get: jest.fn(),
      set: jest.fn()
    } as Partial<CookieService> as jest.Mocked<CookieService>;
    
    mockScanUnitNumberProductCode = {
      resetUnitProductGroup: jest.fn(),
      focusOnUnitNumber: jest.fn(),
      disableUnitProductGroup: jest.fn()
    } as Partial<ScanUnitNumberProductCodeComponent> as jest.Mocked<ScanUnitNumberProductCodeComponent>;

    await TestBed.configureTestingModule({
      imports: [
        VerifyRecoveredPlasmaProductsComponent,
        ApolloTestingModule, 
        NoopAnimationsModule,
        ToastrModule.forRoot(),
      ],
      providers: [
        provideMockStore(),
        {
          provide: ActivatedRoute,
          useValue: {
            paramMap: of({})
          },
        },
        { provide: Router, useValue: mockRouter },
        { provide: ToastrImplService, useValue: mockToastr },
        { provide: ProductIconsService, useValue: mockProductIconsService },
        { provide: RecoveredPlasmaService, useValue: mockRecoveredPlasmaService },
        { provide: CookieService, useValue: mockCookieService }
      ]
    })
    .compileComponents();
    
    mockStore = TestBed.inject(MockStore);
    fixture = TestBed.createComponent(VerifyRecoveredPlasmaProductsComponent);
    component = fixture.componentInstance;
    component.cartonDetails = signal(mockCartonData);
    component.scanUnitNumberProductCode = mockScanUnitNumberProductCode;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call disableInputsIfMaxCartonProduct on ngOnInit', () => {
    jest.spyOn(component, 'disableInputsIfMaxCartonProduct');
    component.ngOnInit();
    expect(component.disableInputsIfMaxCartonProduct).toHaveBeenCalled();
  });

  it('should emit verifyUnitNumberProductCode event when verifyProducts is called', () => {
    jest.spyOn(component.verifyUnitNumberProductCode, 'emit');
    const eventData = { unitNumber: 'W1212112121212', productCode: 'E121212112' };
    component.verifyProducts(eventData);
    expect(component.verifyUnitNumberProductCode.emit).toHaveBeenCalledWith(eventData);
  });

  it('should call resetUnitProductGroup on scanUnitNumberProductCode when resetProductGroup is called', () => {
    component.scanUnitNumberProductCode = mockScanUnitNumberProductCode;
    component.resetProductGroup();
    expect(mockScanUnitNumberProductCode.resetUnitProductGroup).toHaveBeenCalled();
  });

  it('should call focusOnUnitNumber on scanUnitNumberProductCode when focusOnUnitNumber is called', () => {
    component.scanUnitNumberProductCode = mockScanUnitNumberProductCode;
    component.focusOnUnitNumber();
    expect(mockScanUnitNumberProductCode.focusOnUnitNumber).toHaveBeenCalled();
  });

  describe('disableInputsIfMaxCartonProduct', () => {
    it('should call disableProductGroup when canVerify is false', () => {
      jest.spyOn(component, 'disableProductGroup');
      component.cartonDetails = signal({ ...mockCartonData, canVerify: false });
      
      component.disableInputsIfMaxCartonProduct();
      
      expect(component.disableProductGroup).toHaveBeenCalled();
    });

    it('should not call disableProductGroup when canVerify is true', () => {
      jest.spyOn(component, 'disableProductGroup');
      component.cartonDetails = signal({ ...mockCartonData, canVerify: true });
      
      component.disableInputsIfMaxCartonProduct();
      
      expect(component.disableProductGroup).not.toHaveBeenCalled();
    });
  });

  it('should call disableUnitProductGroup on scanUnitNumberProductCode when disableProductGroup is called', () => {
    component.scanUnitNumberProductCode = mockScanUnitNumberProductCode;
    component.disableProductGroup();
    expect(mockScanUnitNumberProductCode.disableUnitProductGroup).toHaveBeenCalled();
  });

  it('should handle undefined scanUnitNumberProductCode in resetProductGroup', () => {
    component.scanUnitNumberProductCode = undefined;
    expect(() => component.resetProductGroup()).not.toThrow();
  });
  
  it('should handle undefined scanUnitNumberProductCode in focusOnUnitNumber', () => {
    component.scanUnitNumberProductCode = undefined;
    expect(() => component.focusOnUnitNumber()).not.toThrow();
  });
  
  it('should handle undefined scanUnitNumberProductCode in disableProductGroup', () => {
    component.scanUnitNumberProductCode = undefined;
    expect(() => component.disableProductGroup()).not.toThrow();
  });
});