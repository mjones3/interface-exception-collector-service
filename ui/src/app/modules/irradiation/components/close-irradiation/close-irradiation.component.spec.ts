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

  beforeEach(async () => {
    const mockRouter = { navigateByUrl: jest.fn() };
    const mockActivatedRoute = { snapshot: { data: { useCheckDigit: true } } };
    const mockMatDialog = { 
      open: jest.fn().mockReturnValue({
        afterClosed: () => of(null)
      })
    };
    const mockToastrService = { success: jest.fn(), error: jest.fn(), warning: jest.fn() };
    const mockFuseConfirmationService = { 
      open: jest.fn().mockReturnValue({
        afterClosed: () => of(true)
      })
    };
    const mockIrradiationService = { 
      submitCentrifugationBatch: jest.fn().mockReturnValue(of({}))
    };
    const mockProcessHeaderService = { setActions: jest.fn() };
    const mockFacilityService = { getFacilityCode: jest.fn().mockReturnValue('TEST') };
    const mockProductIconsService = { getIconByProductFamily: jest.fn().mockReturnValue('icon') };
    const mockChangeDetectorRef = { detectChanges: jest.fn() };

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
        { provide: IrradiationService, useValue: mockIrradiationService },
        { provide: ProcessHeaderService, useValue: mockProcessHeaderService },
        { provide: FacilityService, useValue: mockFacilityService },
        { provide: ProductIconsService, useValue: mockProductIconsService },
        { provide: ChangeDetectorRef, useValue: mockChangeDetectorRef }
      ]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(CloseIrradiationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
