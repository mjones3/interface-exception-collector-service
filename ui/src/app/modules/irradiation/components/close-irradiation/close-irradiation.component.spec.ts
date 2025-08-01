import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ActivatedRoute } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ToastrService } from 'ngx-toastr';
import { FuseConfirmationService } from '../../../../../@fuse/services/confirmation';
import { CloseIrradiationComponent } from './close-irradiation.component';
import { IrradiationService } from '../../services/irradiation.service';
import { ProcessHeaderService, FacilityService } from '@shared';
import { ProductIconsService } from '../../../../shared/services/product-icon.service';
import { Component, NO_ERRORS_SCHEMA } from '@angular/core';
import { of, throwError } from 'rxjs';
import { CookieService } from 'ngx-cookie-service';

@Component({
  selector: 'biopro-record-visual-inspection-modal',
  template: ''
})
class MockRecordVisualInspectionModal {}

describe('CloseIrradiationComponent', () => {
  let component: CloseIrradiationComponent;
  let fixture: ComponentFixture<CloseIrradiationComponent>;
  let irradiationService: any;

  beforeEach(async () => {
    const mockRouter = { navigateByUrl: jest.fn() };
    const mockActivatedRoute = {
      parent: {
        snapshot: {
          data: { initialData: { showCheckDigit: true } }
        }
      }
    };
    const mockMatDialog = {
      open: jest.fn().mockReturnValue({
        afterClosed: () => of({ irradiated: true })
      })
    };
    const mockToastrService = { success: jest.fn(), error: jest.fn(), warning: jest.fn() };
    const mockFuseConfirmationService = {
      open: jest.fn().mockReturnValue({
        afterClosed: () => of(true)
      })
    };
    irradiationService = {
      validateCheckDigit: jest.fn().mockReturnValue(of({ data: { checkDigit: { isValid: true } } })),
      validateDeviceOnCloseBatch: jest.fn()
    };
    const mockProcessHeaderService = { setActions: jest.fn() };
    const mockProductIconsService = { getIconByProductFamily: jest.fn().mockReturnValue('icon') };
    const mockCookieService = { get: jest.fn().mockReturnValue('TEST_FACILITY') };

    await TestBed.configureTestingModule({
      imports: [CloseIrradiationComponent, ReactiveFormsModule, NoopAnimationsModule],
      declarations: [MockRecordVisualInspectionModal],
      schemas: [NO_ERRORS_SCHEMA],
      providers: [
        { provide: Router, useValue: mockRouter },
        { provide: ActivatedRoute, useValue: mockActivatedRoute },
        { provide: MatDialog, useValue: mockMatDialog },
        { provide: ToastrService, useValue: mockToastrService },
        { provide: FuseConfirmationService, useValue: mockFuseConfirmationService },
        { provide: IrradiationService, useValue: irradiationService },
        { provide: ProcessHeaderService, useValue: mockProcessHeaderService },
        { provide: FacilityService, useValue: { getFacilityCode: jest.fn() } },
        { provide: ProductIconsService, useValue: mockProductIconsService },
        { provide: CookieService, useValue: mockCookieService }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CloseIrradiationComponent);
    component = fixture.componentInstance;

    // Mock the unitNumberComponent
    component.unitNumberComponent = {
      reset: jest.fn(),
      focusOnUnitNumber: jest.fn(),
      focusOnCheckDigit: jest.fn(),
      setValidatorsForCheckDigit: jest.fn(),
      form: { disable: jest.fn() },
      controlUnitNumber: { enable: jest.fn(), reset: jest.fn() }
    } as any;

    // Mock the irradiationInput
    component.irradiationInput = {
      focus: jest.fn()
    } as any;

    // Mock form controls - remove the spy to let the real form work
    // jest.spyOn(component.form, 'get').mockReturnValue(mockFormControl as any);

    // Mock methods to avoid errors
    jest.spyOn(component, 'ngAfterViewInit').mockImplementation(() => {});
    jest.spyOn(component, 'focusOnIrradiationInput').mockImplementation(() => {});
    jest.spyOn(component as any, 'showMessage').mockImplementation(() => {});
    jest.spyOn(component as any, 'validateUnitNumber').mockImplementation(() => {});


  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should validate check digit when showCheckDigit is true and not using scanner', () => {
    // Setup
    const event = { unitNumber: 'UNIT001', checkDigit: '5', scanner: false };
    component.showCheckDigit = true;

    // Act
    component.validateUnit(event);

    // Assert
    expect(irradiationService.validateCheckDigit).toHaveBeenCalledWith('UNIT001', '5');
  });

  describe('isSubmitEnabled', () => {
    beforeEach(() => {
      // Mock numberOfUnits getter
      Object.defineProperty(component, 'numberOfUnits', {
        get: jest.fn(() => 2),
        configurable: true
      });
    });

    it('should return false when form is invalid', () => {
      component.form.patchValue({ irradiatorId: null });
      component.products = [{ disabled: false, statuses: [{ value: 'IRRADIATED' }] }] as any;

      expect(component.isSubmitEnabled()).toBe(false);
    });

    it('should return false when numberOfUnits is 0', () => {
      component.form.patchValue({ irradiatorId: 'IRR001' });
      Object.defineProperty(component, 'numberOfUnits', {
        get: jest.fn(() => 0),
        configurable: true
      });

      expect(component.isSubmitEnabled()).toBe(false);
    });

    it('should return true when form is valid, has units, and all enabled products have IRRADIATED status', () => {
      component.form.patchValue({ irradiatorId: 'IRR001' });
      component.products = [
        { disabled: false, statuses: [{ value: 'IRRADIATED' }] },
        { disabled: false, statuses: [{ value: 'IRRADIATED' }] }
      ] as any;

      expect(component.isSubmitEnabled()).toBe(true);
    });

    it('should return true when form is valid, has units, and all enabled products have NOT_IRRADIATED status', () => {
      component.form.patchValue({ irradiatorId: 'IRR001' });
      component.products = [
        { disabled: false, statuses: [{ value: 'NOT IRRADIATED' }] },
        { disabled: false, statuses: [{ value: 'NOT IRRADIATED' }] }
      ] as any;

      expect(component.isSubmitEnabled()).toBe(true);
    });

    it('should return true when form is valid, has units, and enabled products have mixed IRRADIATED/NOT_IRRADIATED status', () => {
      component.form.patchValue({ irradiatorId: 'IRR001' });
      component.products = [
        { disabled: false, statuses: [{ value: 'IRRADIATED' }] },
        { disabled: false, statuses: [{ value: 'NOT IRRADIATED' }] }
      ] as any;

      expect(component.isSubmitEnabled()).toBe(true);
    });

    it('should return false when enabled products do not have IRRADIATED or NOT_IRRADIATED status', () => {
      component.form.patchValue({ irradiatorId: 'IRR001' });
      component.products = [
        { disabled: false, statuses: [{ value: 'PENDING INSPECTION' }] },
        { disabled: false, statuses: [{ value: 'AVAILABLE' }] }
      ] as any;

      expect(component.isSubmitEnabled()).toBe(false);
    });

    it('should ignore disabled products when checking status', () => {
      component.form.patchValue({ irradiatorId: 'IRR001' });
      component.products = [
        { disabled: false, statuses: [{ value: 'IRRADIATED' }] },
        { disabled: true, statuses: [{ value: 'PENDING INSPECTION' }] }
      ] as any;

      expect(component.isSubmitEnabled()).toBe(true);
    });

    it('should return false when some enabled products lack required status', () => {
      component.form.patchValue({ irradiatorId: 'IRR001' });
      component.products = [
        { disabled: false, statuses: [{ value: 'IRRADIATED' }] },
        { disabled: false, statuses: [{ value: 'QUARANTINED' }] }
      ] as any;

      expect(component.isSubmitEnabled()).toBe(false);
    });
  });

  describe('loadIrradiationId', () => {
    beforeEach(() => {
      component.currentLocation = 'TEST_LOCATION';
      jest.spyOn(component as any, 'showMessage').mockImplementation(() => {});
      jest.spyOn(component as any, 'populateIrradiationBatch').mockImplementation(() => {});
    });

    it('should show error when no facility location available', () => {
      component.currentLocation = '';

      component.loadIrradiationId('IRR001');

      expect(component['showMessage']).toHaveBeenCalledWith('ERROR', 'No facility location available');
      expect(irradiationService.validateDeviceOnCloseBatch).not.toHaveBeenCalled();
    });

    it('should call validateDeviceOnCloseBatch with correct parameters', () => {
      const irradiationId = 'IRR001';
      irradiationService.validateDeviceOnCloseBatch.mockReturnValue(of({ data: { validateDeviceOnCloseBatch: [] } }));

      component.loadIrradiationId(irradiationId);

      expect(irradiationService.validateDeviceOnCloseBatch).toHaveBeenCalledWith(irradiationId, 'TEST_LOCATION');
    });

    it('should populate irradiation batch when products are returned', () => {
      const mockProducts = [{ unitNumber: 'UNIT001', productCode: 'PROD001', productDescription: 'Product 1', productFamily: 'Family1' }];
      irradiationService.validateDeviceOnCloseBatch.mockReturnValue(of({ data: { validateDeviceOnCloseBatch: mockProducts } }));
      jest.spyOn(component.unitNumberComponent.controlUnitNumber, 'enable');

      component.loadIrradiationId('IRR001');

      expect(component['populateIrradiationBatch']).toHaveBeenCalled();
      expect(component.unitNumberComponent.controlUnitNumber.enable).toHaveBeenCalled();
    });

    it('should handle service error', () => {
      const error = { message: 'Service error' };
      irradiationService.validateDeviceOnCloseBatch.mockReturnValue(throwError(error));

      component.loadIrradiationId('IRR001');

      expect(component['showMessage']).toHaveBeenCalledWith('ERROR', 'Service error');
    });
  });

  describe('cancel functionality', () => {
    let mockRouter: any;
    let mockFuseConfirmationService: any;

    beforeEach(() => {
      mockRouter = TestBed.inject(Router);
      mockFuseConfirmationService = TestBed.inject(FuseConfirmationService);
      jest.spyOn(component as any, 'resetAllData').mockImplementation(() => {});
    });

    it('should open confirmation dialog when cancel button is clicked', () => {
      component.openCancelConfirmationDialog();

      expect(mockFuseConfirmationService.open).toHaveBeenCalledWith({
        title: 'Confirmation',
        message: 'Products added will be removed from the list without finishing the Irradiation process. Are you sure you want to continue?',
        dismissible: false,
        icon: {
          name: 'heroicons_outline:question-mark-circle',
          show: true,
          color: 'primary',
        },
        actions: {
          confirm: {
            show: true,
          },
          cancel: {
            show: true,
          },
        },
      });
    });

    it('should call resetAllData when confirmation dialog is confirmed', () => {
      mockFuseConfirmationService.open.mockReturnValue({
        afterClosed: () => of(true)
      });

      component.openCancelConfirmationDialog();

      expect(component['resetAllData']).toHaveBeenCalled();
    });

    it('should not call resetAllData when confirmation dialog is cancelled', () => {
      mockFuseConfirmationService.open.mockReturnValue({
        afterClosed: () => of(false)
      });

      component.openCancelConfirmationDialog();

      expect(component['resetAllData']).not.toHaveBeenCalled();
    });

    it('should reset all component state when resetAllData is called', () => {
      // Remove the mock for this specific test
      (component as any).resetAllData.mockRestore();
      
      // Setup initial state
      component.deviceId = true;
      component.products = [{ unitNumber: 'UNIT001' }] as any;
      component.initialProductsState = [{ unitNumber: 'UNIT001' }] as any;
      component.selectedProducts = [{ unitNumber: 'UNIT001' }] as any;
      component.allProducts = [{ unitNumber: 'UNIT001' }] as any;
      component.currentDateTime = '01/01/2024 10:00';

      const controlResetSpy = jest.spyOn(component.unitNumberComponent.controlUnitNumber, 'reset');
      const irradiationResetSpy = jest.spyOn(component.irradiation, 'reset');
      const irradiationEnableSpy = jest.spyOn(component.irradiation, 'enable');

      component['resetAllData']();

      expect(component.deviceId).toBe(false);
      expect(component.products).toEqual([]);
      expect(component.initialProductsState).toEqual([]);
      expect(component.selectedProducts).toEqual([]);
      expect(component.allProducts).toEqual([]);
      expect(component.currentDateTime).toBe('');
      expect(controlResetSpy).toHaveBeenCalled();
      expect(irradiationResetSpy).toHaveBeenCalled();
      expect(irradiationEnableSpy).toHaveBeenCalled();
    });
  });
});
