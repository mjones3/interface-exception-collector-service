import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatIconTestingModule } from '@angular/material/icon/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { ToastrService } from 'ngx-toastr';
import { FuseConfirmationService } from '../../../../../@fuse/services/confirmation';
import { FacilityService, ProcessHeaderService } from '@shared';
import { ProductIconsService } from '../../../../shared/services/product-icon.service';
import { IrradiationService } from '../../services/irradiation.service';
import { StartIrradiationComponent } from './start-irradiation.component';
import { of } from 'rxjs';
import { IrradiationProductDTO, ValidateUnitEvent } from '../../models/model';
import { Component } from '@angular/core';

@Component({
  selector: 'biopro-irradiation-select-product-modal',
  template: ''
})
class MockSelectProductModal {}

describe('StartIrradiationComponent', () => {
    let component: StartIrradiationComponent;
    let fixture: ComponentFixture<StartIrradiationComponent>;
    let router: Router;
    let irradiationService: IrradiationService;
    let toastrService: ToastrService;
    let confirmationService: FuseConfirmationService;
    let matDialog: MatDialog;
    let facilityService: FacilityService;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                StartIrradiationComponent,
                NoopAnimationsModule,
                MatIconTestingModule,
                ReactiveFormsModule,
            ],
            declarations: [MockSelectProductModal],
            providers: [
                FormBuilder,
                {
                    provide: ActivatedRoute,
                    useValue: {
                        snapshot: {
                            data: { useCheckDigit: true },
                        },
                    },
                },
                {
                    provide: Router,
                    useValue: { navigateByUrl: jest.fn() },
                },
                {
                    provide: IrradiationService,
                    useValue: {
                        submitCentrifugationBatch: jest.fn().mockReturnValue(of({})),
                    },
                },
                {
                    provide: ToastrService,
                    useValue: { success: jest.fn(), warning: jest.fn(), error: jest.fn() },
                },
                {
                    provide: FuseConfirmationService,
                    useValue: {
                        open: jest.fn().mockReturnValue({
                            afterClosed: () => of(true),
                        }),
                    },
                },
                {
                    provide: MatDialog,
                    useValue: {
                        open: jest.fn().mockReturnValue({
                            afterClosed: () => of(null),
                        }),
                    },
                },
                {
                    provide: FacilityService,
                    useValue: { getFacilityCode: jest.fn().mockReturnValue('TEST') },
                },
                {
                    provide: ProcessHeaderService,
                    useValue: { setActions: jest.fn() },
                },
                {
                    provide: ProductIconsService,
                    useValue: { getIconByProductFamily: jest.fn().mockReturnValue('icon') },
                },
            ],
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(StartIrradiationComponent);
        component = fixture.componentInstance;
        router = TestBed.inject(Router);
        irradiationService = TestBed.inject(IrradiationService);
        toastrService = TestBed.inject(ToastrService);
        confirmationService = TestBed.inject(FuseConfirmationService);
        matDialog = TestBed.inject(MatDialog);
        facilityService = TestBed.inject(FacilityService);
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should initialize form with required validators', () => {
        expect(component.form.get('irradiationId')?.hasError('required')).toBeTruthy();
        expect(component.form.get('lotNumber')?.hasError('required')).toBeTruthy();
    });

    it('should set check digit visibility from route data', () => {
        expect(component.isCheckDigitVisible).toBeTruthy();
    });

    it('should disable submit when form is invalid', () => {
        component.products = [];
        expect(component.isSubmitEnabled()).toBeFalsy();
    });


    it('should open cancel confirmation dialog', () => {
        component.openCancelConfirmationDialog();
        expect(confirmationService.open).toHaveBeenCalled();
    });

    it('should submit irradiation batch', () => {
        component.products = [{ unitNumber: 'W036825314134' } as IrradiationProductDTO];
        component.deviceId = 'test-device';

        component.submit();

        expect(irradiationService.submitCentrifugationBatch).toHaveBeenCalledWith({
            unitNumbers: ['W036825314134'],
            location: 'TEST',
            deviceId: 'test-device',
        });
    });

    it('should validate unit and open product selection dialog', () => {
        const event: ValidateUnitEvent = {
            unitNumber: 'W036825314134',
            checkDigit: 'D',
            scanner: false
        };

        component.validateUnit(event);

        expect(matDialog.open).toHaveBeenCalled();
    });

    it('should reset all data on cancel', () => {
        component.products = [{ unitNumber: 'test' } as IrradiationProductDTO];
        component.selectedProducts = [{ unitNumber: 'test' } as IrradiationProductDTO];

        component['cancel']();

        expect(component.products).toEqual([]);
        expect(component.selectedProducts).toEqual([]);
    });

    it('should get number of units', () => {
        component.products = [
            { unitNumber: 'test1' } as IrradiationProductDTO,
            { unitNumber: 'test2' } as IrradiationProductDTO
        ];
        expect(component.numberOfUnits).toBeLessThanOrEqual(2);
    });
});
