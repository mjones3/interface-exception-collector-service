import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ActivatedRoute } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { provideAnimations } from '@angular/platform-browser/animations';
import { ToastrService } from 'ngx-toastr';
import { FuseConfirmationService } from '../../../../../@fuse/services/confirmation';
import { CloseIrradiationComponent } from './close-irradiation.component';
import { IrradiationService } from '../../services/irradiation.service';
import { ProcessHeaderService, FacilityService } from '@shared';
import { ProductIconsService } from '../../../../shared/services/product-icon.service';
import { ChangeDetectorRef, Component } from '@angular/core';
import { of } from 'rxjs';

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
      validateCheckDigit: jest.fn().mockReturnValue(of({ data: { checkDigit: { isValid: true } } }))
    };
    const mockProcessHeaderService = { setActions: jest.fn() };
    const mockProductIconsService = { getIconByProductFamily: jest.fn().mockReturnValue('icon') };

    await TestBed.configureTestingModule({
      imports: [CloseIrradiationComponent, ReactiveFormsModule],
      declarations: [MockRecordVisualInspectionModal],
      providers: [
        provideAnimations(),
        { provide: Router, useValue: mockRouter },
        { provide: ActivatedRoute, useValue: mockActivatedRoute },
        { provide: MatDialog, useValue: mockMatDialog },
        { provide: ToastrService, useValue: mockToastrService },
        { provide: FuseConfirmationService, useValue: mockFuseConfirmationService },
        { provide: IrradiationService, useValue: irradiationService },
        { provide: ProcessHeaderService, useValue: mockProcessHeaderService },
        { provide: FacilityService, useValue: { getFacilityCode: jest.fn() } },
        { provide: ProductIconsService, useValue: mockProductIconsService }
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
      controlUnitNumber: { enable: jest.fn() }
    } as any;
    
    // Mock methods to avoid errors
    jest.spyOn(component, 'ngAfterViewInit').mockImplementation(() => {});
    jest.spyOn(component as any, 'showMessage').mockImplementation(() => {});
    jest.spyOn(component as any, 'validateUnitNumber').mockImplementation(() => {});
    
    fixture.detectChanges();
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
});
