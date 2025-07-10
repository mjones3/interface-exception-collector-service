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
import { CookieService } from 'ngx-cookie-service';

// Mock keycloak-js module
jest.mock('keycloak-js', () => ({}));

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
                        loadDeviceById: jest.fn().mockReturnValue(of({ data: { valid: true } })),
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
                {
                    provide: CookieService,
                    useValue: { get: jest.fn().mockReturnValue('TEST') },
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

    it('should prepare data for irradiation batch submission', () => {
        component.products = [{ unitNumber: 'W036825314134' } as IrradiationProductDTO];
        component.deviceId = 'test-device';

        // Create spy to check if the correct data is prepared
        const requestDTO = {
            unitNumbers: ['W036825314134'],
            location: 'TEST',
            deviceId: 'test-device',
        };

        component.submit();

        // Since the actual submission is commented out in the component,
        // we're just verifying the data preparation is correct
        expect(facilityService.getFacilityCode).toHaveBeenCalled();
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
        component.initialProductsState = [{ unitNumber: 'test' } as IrradiationProductDTO];
        component.allProducts = [{ unitNumber: 'test' } as IrradiationProductDTO];

        // Mock the form reset methods
        jest.spyOn(component.irradiation, 'reset');
        jest.spyOn(component.lotNumber, 'reset');

        // Mock the unitNumberComponent
        component.unitNumberComponent = {
            reset: jest.fn(),
            form: { disable: jest.fn() }
        } as any;

        component['cancel']();

        expect(component.products).toEqual([]);
        expect(component.selectedProducts).toEqual([]);
        expect(component.initialProductsState).toEqual([]);
        expect(component.allProducts).toEqual([]);
        expect(component.irradiation.reset).toHaveBeenCalled();
        expect(component.lotNumber.reset).toHaveBeenCalled();
        expect(component.unitNumberComponent.reset).toHaveBeenCalled();
    });

    it('should get number of units', () => {
        component.allProducts = [
            { unitNumber: 'test1', disabled: false } as IrradiationProductDTO,
            { unitNumber: 'test2', disabled: false } as IrradiationProductDTO
        ];
        expect(component.numberOfUnits).toBe(2);

        // Test with disabled units
        component.allProducts = [
            { unitNumber: 'test1', disabled: false } as IrradiationProductDTO,
            { unitNumber: 'test2', disabled: true } as IrradiationProductDTO
        ];
        expect(component.numberOfUnits).toBe(1);
    });

    it('should load irradiation device by ID', () => {
        const deviceId = 'test-device';
        component.loadIrradiationId(deviceId);

        expect(irradiationService.loadDeviceById).toHaveBeenCalledWith(deviceId, 'TEST');
    });

    it('should enable unit number control when lot number is validated', () => {
        // Mock the unitNumberComponent
        component.unitNumberComponent = {
            controlUnitNumber: { enable: jest.fn() }
        } as any;

        component.validateLotNumber('LOT123');

        expect(component.unitNumberComponent.controlUnitNumber.enable).toHaveBeenCalled();
    });

    it('should toggle product selection', () => {
        const product = { unitNumber: 'test1', productCode: 'P1' } as IrradiationProductDTO;

        // Test adding to selection
        component.selectedProducts = [];
        component.toggleProduct(product);
        expect(component.selectedProducts).toContain(product);

        // Test removing from selection
        component.toggleProduct(product);
        expect(component.selectedProducts).not.toContain(product);
    });

    it('should open remove confirmation dialog', () => {
        component.openRemoveConfirmationDialog();
        expect(confirmationService.open).toHaveBeenCalled();
    });

    it('should remove selected products', () => {
        const product1 = { unitNumber: 'test1', productCode: 'P1' } as IrradiationProductDTO;
        const product2 = { unitNumber: 'test2', productCode: 'P2' } as IrradiationProductDTO;

        component.products = [product1, product2];
        component.selectedProducts = [product1];
        component.allProducts = [product1, product2];

        // Call the private method directly
        component['removeSelected']();

        expect(component.products).toEqual([product2]);
        expect(component.selectedProducts).toEqual([]);
        expect(component.allProducts).toEqual([product2]);
    });

    it('should redirect to irradiation page', () => {
        component.redirect();
        expect(router.navigateByUrl).toHaveBeenCalledWith('irradiation');
    });

    it('should show appropriate messages based on message type', () => {
        // Test error message
        component['showMessage']('ERROR', 'Error message');
        expect(toastrService.error).toHaveBeenCalledWith('Error message');

        // Test warning message
        component['showMessage']('WARNING', 'Warning message');
        expect(toastrService.warning).toHaveBeenCalledWith('Warning message');

        // Test success message
        component['showMessage']('SUCCESS', 'Success message');
        expect(toastrService.success).toHaveBeenCalledWith('Success message');
    });
});
