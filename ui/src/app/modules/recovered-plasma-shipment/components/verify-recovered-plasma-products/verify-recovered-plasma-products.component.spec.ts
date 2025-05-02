import { ComponentFixture, TestBed } from '@angular/core/testing';
import { VerifyRecoveredPlasmaProductsComponent } from './verify-recovered-plasma-products.component';
import { MockStore, provideMockStore } from '@ngrx/store/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { of } from 'rxjs';
import { ToastrModule } from 'ngx-toastr';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ScanUnitNumberProductCodeComponent } from 'app/shared/components/scan-unit-number-product-code/scan-unit-number-product-code.component';
import { CookieService } from 'ngx-cookie-service';
import { ToastrImplService } from '@shared';
import { ProductIconsService } from 'app/shared/services/product-icon.service';
import { RecoveredPlasmaService } from '../../services/recovered-plasma.service';
import { MatDividerModule } from '@angular/material/divider';

describe('VerifyRecoveredPlasmaProductsComponent', () => {
  let component: VerifyRecoveredPlasmaProductsComponent;
  let fixture: ComponentFixture<VerifyRecoveredPlasmaProductsComponent>;
  let mockStore: MockStore;
  let mockRecoveredPlasmaService: jest.Mocked<RecoveredPlasmaService>;
  let mockRouter: jest.Mocked<Router>;
  let mockToastr: jest.Mocked<ToastrImplService>;
  let mockProductIconsService: jest.Mocked<ProductIconsService>;
  let mockCookieService: jest.Mocked<CookieService>;


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

    await TestBed.configureTestingModule({
      declarations: [],
      imports: [
        NoopAnimationsModule,
        ToastrModule.forRoot(),
        MatDividerModule
      ],
      providers: [
        provideMockStore(),
        {
          provide: ActivatedRoute,
          useValue: {
            paramMap: of({}),
            snapshot: {
              params: {}
            }
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
    fixture.detectChanges();
    // Set the scanUnitNumberProductCode reference
    component.scanUnitNumberProductCode = {
      resetUnitProductGroup: jest.fn(),
      focusOnUnitNumber: jest.fn(),
      disableUnitProductGroup: jest.fn()
    } as Partial<ScanUnitNumberProductCodeComponent> as ScanUnitNumberProductCodeComponent;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should emit verifyUnitNumberProductCode event when verifyProducts is called', () => {
    jest.spyOn(component.verifyUnitNumberProductCode, 'emit');
    const eventData = { unitNumber: 'W1212112121212', productCode: 'E121212' };
    component.verifyProducts(eventData);
    expect(component.verifyUnitNumberProductCode.emit).toHaveBeenCalledWith(eventData);
  });

  it('should call resetUnitProductGroup on scanUnitNumberProductCode when resetProductGroup is called', () => {
    component.resetProductGroup();
    expect(component.scanUnitNumberProductCode.resetUnitProductGroup).toHaveBeenCalled();
  });

  it('should call focusOnUnitNumber on scanUnitNumberProductCode when focusOnUnitNumber is called', () => {
    component.focusOnUnitNumber();
    expect(component.scanUnitNumberProductCode.focusOnUnitNumber).toHaveBeenCalled();
  });

  it('should get the correct icon for a product type', () => {
    const productType = 'PLASMA';
    mockProductIconsService.getIconByProductFamily.mockReturnValue('plasma-icon');
    const result = component.getItemIcon(productType);
    expect(mockProductIconsService.getIconByProductFamily).toHaveBeenCalledWith(productType);
    expect(result).toBe('plasma-icon');
  });
});