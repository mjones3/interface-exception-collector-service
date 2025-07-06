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

describe('CloseIrradiationComponent', () => {
  let component: CloseIrradiationComponent;
  let fixture: ComponentFixture<CloseIrradiationComponent>;

  beforeEach(async () => {
    const mockRouter = { navigateByUrl: jest.fn() };
    const mockActivatedRoute = { snapshot: { data: { useCheckDigit: true } } };
    const mockMatDialog = { open: jest.fn() };
    const mockToastrService = { success: jest.fn(), error: jest.fn(), warning: jest.fn() };
    const mockFuseConfirmationService = { open: jest.fn() };
    const mockIrradiationService = { submitCentrifugationBatch: jest.fn() };
    const mockProcessHeaderService = { setActions: jest.fn() };
    const mockFacilityService = { getFacilityCode: jest.fn().mockReturnValue('TEST') };
    const mockProductIconsService = { getIconByProductFamily: jest.fn() };

    await TestBed.configureTestingModule({
      imports: [CloseIrradiationComponent, ReactiveFormsModule],
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
        { provide: ProductIconsService, useValue: mockProductIconsService }
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
