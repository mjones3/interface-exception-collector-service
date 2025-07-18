import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ActivatedRoute } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { provideAnimations } from '@angular/platform-browser/animations';
import { ToastrService } from 'ngx-toastr';
import { FuseConfirmationService } from '../../../../../@fuse/services/confirmation';
import { StartIrradiationComponent } from './start-irradiation.component';
import { IrradiationService } from '../../services/irradiation.service';
import { ProcessHeaderService, ScanUnitNumberCheckDigitComponent } from '@shared';
// Mock FuseIconsService since we don't have access to the actual implementation
class MockFuseIconsService {
  getIcon = jest.fn().mockReturnValue(of('<svg>mock-icon</svg>'));
  addIconResolver = jest.fn();
  registerIcons = jest.fn();
}
import { ProductIconsService } from '../../../../shared/services/product-icon.service';
import { Component } from '@angular/core';
import { EMPTY, Observable, of, throwError } from 'rxjs';
import { CookieService } from 'ngx-cookie-service';
import { DiscardService } from '../../../../shared/services/discard.service';
import { InputComponent } from '../../../../shared/components/input/input.component';

@Component({
  selector: 'biopro-scan-unit-number-check-digit',
  template: ''
})
class MockScanUnitNumberCheckDigitComponent {
  form = { disable: jest.fn() };
  controlUnitNumber = { reset: jest.fn(), enable: jest.fn() };
  reset = jest.fn();
  focusOnUnitNumber = jest.fn();
  focusOnCheckDigit = jest.fn();
  setValidatorsForCheckDigit = jest.fn();
}

@Component({
  selector: 'biopro-input',
  template: ''
})
class MockInputComponent {
  focus = jest.fn();
}

@Component({
  selector: 'biopro-irradiation-select-product-modal',
  template: ''
})
class MockIrradiationSelectProductModal {}

describe('StartIrradiationComponent', () => {
  let component: StartIrradiationComponent;
  let fixture: ComponentFixture<StartIrradiationComponent>;
  let mockRouter, mockMatDialog, mockToastrService, mockFuseConfirmationService,
      mockIrradiationService, mockDiscardService;

  beforeEach(async () => {
    mockRouter = { navigateByUrl: jest.fn() };
    const mockActivatedRoute = {
      parent: {
        snapshot: {
          data: {
            initialData: {
              showCheckDigit: true
            }
          }
        }
      }
    };
    mockMatDialog = {
      open: jest.fn().mockReturnValue({
        afterClosed: () => of(null)
      })
    };
    mockToastrService = { success: jest.fn(), error: jest.fn(), warning: jest.fn() };
    mockFuseConfirmationService = {
      open: jest.fn().mockReturnValue({
        afterClosed: () => of(true)
      })
    };
    mockIrradiationService = {
      validateCheckDigit: jest.fn(),
      validateUnitNumber: jest.fn(),
      startIrradiationSubmitBatch: jest.fn(),
      loadDeviceById: jest.fn()
    };

    // Set default return values
    mockIrradiationService.validateCheckDigit.mockReturnValue(of({ data: { checkDigit: { isValid: true } } }));
    mockIrradiationService.validateUnitNumber.mockReturnValue(of({ data: { validateUnit: [] } }));
    mockIrradiationService.startIrradiationSubmitBatch.mockReturnValue(of({ data: { submitBatch: { message: 'Success' } } }));
    mockIrradiationService.loadDeviceById.mockReturnValue(of({ data: { validateDevice: true } }));

    const mockProcessHeaderService = { setActions: jest.fn() };
    const mockProductIconsService = { getIconByProductFamily: jest.fn().mockReturnValue('icon') };
    const mockCookieService = { get: jest.fn().mockReturnValue('TEST_FACILITY') };
    mockDiscardService = { discardProduct: jest.fn().mockReturnValue(of({})) };
    const mockIconsService = new MockFuseIconsService();
    // Register the icons that are used in the component
    mockIconsService.registerIcons = jest.fn().mockImplementation(() => {});
    mockIconsService.addIconResolver = jest.fn().mockImplementation(() => {});
    // Ensure the icon service can handle the specific icons used in error messages
    mockIconsService.getIcon.mockImplementation((name) => {
      if (name === 'heroicons_outline:x-circle') {
        return of('<svg>mock-x-circle-icon</svg>');
      }
      if (name === 'heroicons_outline:question-mark-circle') {
        return of('<svg>mock-question-mark-icon</svg>');
      }
      return of('<svg>mock-icon</svg>');
    });

    await TestBed.configureTestingModule({
      imports: [StartIrradiationComponent, ReactiveFormsModule],
      declarations: [MockIrradiationSelectProductModal, MockScanUnitNumberCheckDigitComponent, MockInputComponent],
      providers: [
        provideAnimations(),
        { provide: Router, useValue: mockRouter },
        { provide: ActivatedRoute, useValue: mockActivatedRoute },
        { provide: MatDialog, useValue: mockMatDialog },
        { provide: ToastrService, useValue: mockToastrService },
        { provide: FuseConfirmationService, useValue: mockFuseConfirmationService },
        { provide: IrradiationService, useValue: mockIrradiationService },
        { provide: ProcessHeaderService, useValue: mockProcessHeaderService },
        { provide: ProductIconsService, useValue: mockProductIconsService },
        { provide: CookieService, useValue: mockCookieService },
        { provide: DiscardService, useValue: mockDiscardService },
        { provide: ScanUnitNumberCheckDigitComponent, useClass: MockScanUnitNumberCheckDigitComponent },
        { provide: InputComponent, useClass: MockInputComponent },
        { provide: 'FuseIconsService', useValue: mockIconsService },
        // Add a mock for any icon service that might be used
        { provide: 'IconsService', useValue: mockIconsService }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(StartIrradiationComponent);
    component = fixture.componentInstance;

    // Mock ViewChild components
    Object.defineProperty(component, 'unitNumberComponent', {
      value: {
        form: { disable: jest.fn() },
        controlUnitNumber: { reset: jest.fn(), enable: jest.fn() },
        reset: jest.fn(),
        focusOnUnitNumber: jest.fn(),
        focusOnCheckDigit: jest.fn(),
        setValidatorsForCheckDigit: jest.fn()
      },
      configurable: true,
      writable: true
    });

    fixture.detectChanges();
  });

  const createTestProduct = (unitNumber = 'UNIT-001', productCode = 'PROD-001', order = 1) => ({
    unitNumber,
    productCode,
    disabled: false,
    productDescription: `Test Product ${order}`,
    status: 'AVAILABLE',
    productFamily: 'TEST',
    icon: 'test-icon',
    order,
    statuses: [{ value: 'AVAILABLE', classes: 'bg-green-500 text-white' }],
    location: 'TEST_FACILITY',
    comments: '',
    statusReason: null,
    unsuitableReason: null,
    expired: null,
    alreadyIrradiated: null,
    notConfigurableForIrradiation: null,
    quarantines: null
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with required fields', () => {
    expect(component.form).toBeDefined();
    expect(component.form.get('irradiatorId')).toBeDefined();
    expect(component.form.get('lotNumber')).toBeDefined();
  });

  it('should disable submit button when form is invalid', () => {
    component.form.patchValue({
      irradiatorId: null,
      lotNumber: null
    });
    expect(component.isSubmitEnabled()).toBeFalsy();
  });

  it('should disable submit button when no units are added', () => {
    component.form.patchValue({
      irradiatorId: 'IRR-001',
      lotNumber: 'LOT-001'
    });
    component.allProducts = [];
    expect(component.isSubmitEnabled()).toBeFalsy();
  });

  it('should enable submit button when form is valid and units are added', () => {
    component.form.patchValue({
      irradiatorId: 'IRR-001',
      lotNumber: 'LOT-001'
    });
    component.allProducts = [createTestProduct()];
    expect(component.isSubmitEnabled()).toBeTruthy();
  });

  it('should load irradiation device when valid ID is provided', () => {
    component.loadIrradiationId('IRR-001');
    expect(mockIrradiationService.loadDeviceById).toHaveBeenCalledWith('IRR-001', component.currentLocation);
  });

  it('should handle error when loading irradiation device', () => {
    // Setup error response
    mockIrradiationService.loadDeviceById.mockReturnValueOnce(
      throwError(() => ({ message: 'Error loading device' }))
    );

    // Call method
    component.loadIrradiationId('IRR-001');

    // Verify error was shown
    expect(mockToastrService.error).toHaveBeenCalledWith('Error loading device');
  });

  it('should reset all data when cancel confirmation dialog is confirmed', () => {
    // Setup initial state
    const testProduct = createTestProduct();
    component.products = [testProduct];
    component.initialProductsState = [testProduct];
    component.selectedProducts = [testProduct];
    component.allProducts = [testProduct];

    // Call openCancelConfirmationDialog which eventually calls cancel
    component.openCancelConfirmationDialog();

    // Verify dialog was opened
    expect(mockFuseConfirmationService.open).toHaveBeenCalled();

    // Since our mock returns true for afterClosed, the cancel method should be called
    // which resets all data
    expect(component.products).toEqual([]);
    expect(component.initialProductsState).toEqual([]);
    expect(component.selectedProducts).toEqual([]);
    expect(component.allProducts).toEqual([]);
  });

  it('should submit batch when form is valid and units are added', () => {
    // Setup form and products
    component.form.patchValue({
      irradiatorId: 'IRR-001',
      lotNumber: 'LOT-001'
    });
    component.allProducts = [createTestProduct()];
    component.products = [...component.allProducts];
    component.startTime = '2023-01-01T12:00:00';

    // Call submit
    component.submit();

    // Verify service was called with correct parameters
    expect(mockIrradiationService.startIrradiationSubmitBatch).toHaveBeenCalledWith({
      deviceId: 'IRR-001',
      startTime: '2023-01-01T12:00:00',
      batchItems: [
        {
          unitNumber: 'UNIT-001',
          productCode: 'PROD-001',
          lotNumber: 'LOT-001'
        }
      ]
    });
  });

  it('should handle error when submitting batch', () => {
    // Setup form and products
    component.form.patchValue({
      irradiatorId: 'IRR-001',
      lotNumber: 'LOT-001'
    });
    component.allProducts = [createTestProduct()];
    component.products = [...component.allProducts];
    component.startTime = '2023-01-01T12:00:00';

    // Setup error response
    mockIrradiationService.startIrradiationSubmitBatch.mockReturnValueOnce(
      throwError(() => ({ message: 'Submission error' }))
    );

    // Call submit
    component.submit();

    // Verify error was shown
    expect(mockToastrService.error).toHaveBeenCalledWith('Submission error');
  });

  it('should validate unit number with check digit', () => {
    // Setup
    component.currentLocation = 'TEST_FACILITY';
    component.showCheckDigit = true;

    // Call validateUnit
    component.validateUnit({ unitNumber: 'UNIT-001', checkDigit: '5', scanner: false });

    // Verify services were called
    expect(mockIrradiationService.validateCheckDigit).toHaveBeenCalledWith('UNIT-001', '5');
    expect(mockIrradiationService.validateUnitNumber).toHaveBeenCalledWith('UNIT-001', 'TEST_FACILITY');
  });

  it('should validate unit number without check digit when scanner is used', () => {
    // Setup
    component.currentLocation = 'TEST_FACILITY';
    component.showCheckDigit = true;

    // Call validateUnit with scanner=true
    component.validateUnit({ unitNumber: 'UNIT-001', checkDigit: '5', scanner: true });

    // Verify only validateUnitNumber was called
    expect(mockIrradiationService.validateCheckDigit).not.toHaveBeenCalled();
    expect(mockIrradiationService.validateUnitNumber).toHaveBeenCalledWith('UNIT-001', 'TEST_FACILITY');
  });

  it('should handle invalid check digit', () => {
    // Setup
    component.currentLocation = 'TEST_FACILITY';
    component.showCheckDigit = true;

    // Create a new mock for validateCheckDigit that returns invalid first
    const validateCheckDigitMock = jest.fn();
    validateCheckDigitMock.mockReturnValue(of({ data: { checkDigit: { isValid: false } } }));

    // Save original mock and replace it
    const originalValidateCheckDigit = mockIrradiationService.validateCheckDigit;
    mockIrradiationService.validateCheckDigit = validateCheckDigitMock;

    // Ensure the mock component is properly set up
    Object.defineProperty(component, 'unitNumberComponent', {
      value: {
        form: { disable: jest.fn() },
        controlUnitNumber: { reset: jest.fn(), enable: jest.fn() },
        reset: jest.fn(),
        focusOnUnitNumber: jest.fn(),
        focusOnCheckDigit: jest.fn(),
        setValidatorsForCheckDigit: jest.fn()
      },
      configurable: true,
      writable: true
    });

    // Call validateUnit
    component.validateUnit({ unitNumber: 'UNIT-001', checkDigit: '5', scanner: false });

    // Verify error handling
    expect(mockToastrService.error).toHaveBeenCalledWith('Invalid check digit');
    expect(component.unitNumberComponent.setValidatorsForCheckDigit).toHaveBeenCalledWith(false);
    expect(component.unitNumberComponent.focusOnCheckDigit).toHaveBeenCalled();

    // Restore original mock
    mockIrradiationService.validateCheckDigit = originalValidateCheckDigit;
  });

  it('should handle validation error', () => {
    // Setup
    component.currentLocation = 'TEST_FACILITY';
    component.showCheckDigit = false; // Skip check digit validation

    // Setup error response
    mockIrradiationService.validateUnitNumber.mockReturnValueOnce(
      throwError(() => ({ message: 'Validation error' }))
    );

    // Call validateUnit
    component.validateUnit({ unitNumber: 'UNIT-001', checkDigit: null, scanner: true });

    // Verify error was shown
    expect(mockToastrService.error).toHaveBeenCalledWith('Validation error');
  });

  it('should toggle product selection', () => {
    // Setup
    const product = createTestProduct();
    component.selectedProducts = [];

    // Add product
    component.toggleProduct(product);
    expect(component.selectedProducts).toContain(product);

    // Remove product
    component.toggleProduct(product);
    expect(component.selectedProducts).not.toContain(product);
  });

  it('should select all units', () => {
    // Setup
    const product1 = createTestProduct('UNIT-001', 'PROD-001', 1);
    const product2 = createTestProduct('UNIT-002', 'PROD-002', 2);
    component.products = [product1, product2];
    component.selectedProducts = [];

    // Select all
    component.selectAllUnits();
    expect(component.selectedProducts.length).toBe(2);

    // Deselect all
    component.selectAllUnits();
    expect(component.selectedProducts.length).toBe(0);
  });

  it('should remove selected products', () => {
    // Setup
    const product1 = createTestProduct('UNIT-001', 'PROD-001', 1);
    const product2 = createTestProduct('UNIT-002', 'PROD-002', 2);
    component.products = [product1, product2];
    component.allProducts = [product1, product2];
    component.selectedProducts = [product1];

    // Call openRemoveConfirmationDialog
    component.openRemoveConfirmationDialog();

    // Verify dialog was opened
    expect(mockFuseConfirmationService.open).toHaveBeenCalled();

    // Since our mock returns true for afterClosed, the removeSelected method should be called
    expect(component.products.length).toBe(1);
    expect(component.products[0]).toBe(product2);
    expect(component.allProducts.length).toBe(1);
    expect(component.selectedProducts.length).toBe(0);
  });

  it('should focus on irradiation input', () => {
    // Setup
    component.irradiationInput = { focus: jest.fn() } as any;

    // Call method
    component.focusOnIrradiationInput();

    // Verify focus was called
    expect(component.irradiationInput.focus).toHaveBeenCalled();
  });

  it('should focus on lot number input', () => {
    // Setup
    component.lotNumberInput = { focus: jest.fn() } as any;

    // Call method
    component.focusOnLotNumberInput();

    // Verify focus was called
    expect(component.lotNumberInput.focus).toHaveBeenCalled();
  });

  it('should redirect to start-irradiation page', () => {
    // Call redirect
    component.redirect();

    // Verify navigation
    expect(mockRouter.navigateByUrl).toHaveBeenCalledWith('irradiation/start-irradiation');
  });
});
