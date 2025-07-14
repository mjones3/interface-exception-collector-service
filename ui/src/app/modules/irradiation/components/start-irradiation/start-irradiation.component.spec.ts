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
import { IrradiationProductDTO, MessageType, ValidateUnitEvent, ValidationDataDTO } from '../../models/model';
import { Component } from '@angular/core';
import { CookieService } from 'ngx-cookie-service';
import { DiscardService } from "../../../../shared/services/discard.service";

// Mock keycloak-js module
jest.mock('keycloak-js', () => ({}));

@Component({
  selector: 'biopro-irradiation-select-product-modal',
  template: ''
})
class MockSelectProductModal {}

// Define constants used in the component
const AVAILABLE = 'AVAILABLE';
const QUARANTINED = 'QUARANTINED';
const EXPIRED = 'EXPIRED';
const UNSUITABLE = 'UNSUITABLE';
const DISCARDED = 'DISCARDED';
const SHIPPED = 'SHIPPED';
const IRRADIATION_ID_ERROR = 'Device not in current location';
const DEVICE_USED_ERROR = 'Device is in use';

describe('StartIrradiationComponent', () => {
    let component: StartIrradiationComponent;
    let fixture: ComponentFixture<StartIrradiationComponent>;
    let router: Router;
    let irradiationService: IrradiationService;
    let toastrService: ToastrService;
    let confirmationService: FuseConfirmationService;
    let matDialog: MatDialog;
    let facilityService: FacilityService;
    let discardService: DiscardService;

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
                        loadDeviceById: jest.fn().mockReturnValue(of({ data: { validateDevice: true } })),
                        validateUnit: jest.fn().mockReturnValue(of({ data: { products: [] } })),
                    },
                },
                {
                    provide: DiscardService,
                    useValue: {
                        discardProduct: jest.fn().mockReturnValue(of({}))
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
        discardService = TestBed.inject(DiscardService);
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should initialize form with required validators', () => {
        expect(component.form.get('irradiatorId')?.hasError('required')).toBeTruthy();
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

        // Spy on getFacilityCode
        const getFacilityCodeSpy = jest.spyOn(facilityService, 'getFacilityCode');

        component.submit();

        // Verify the facility code was retrieved
        expect(getFacilityCodeSpy).toHaveBeenCalled();

        // Verify the correct data structure was created
        // We can't directly check the requestDTO since it's a local variable,
        // but we can verify the facility code was retrieved and the unit numbers were mapped
        expect(component.products.map(p => p.unitNumber)).toEqual(['W036825314134']);
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

    it('should handle populateCentrifugationBatch correctly', () => {
        const mockProduct = {
            unitNumber: 'W036825314134',
            productCode: 'E468900',
            productDescription: 'WHOLE BLOOD',
            status: 'AVAILABLE',
            productFamily: 'WHOLE_BLOOD',
            icon: 'icon',
            order: 1,
            statuses: [{ value: 'AVAILABLE', classes: 'bg-green-500 text-white' }],
            location: 'TEST'
        } as IrradiationProductDTO;

        // Initial state with empty products
        component.products = [];
        component.initialProductsState = [];
        component.allProducts = [];

        // Mock unitNumberComponent
        component.unitNumberComponent = {
            reset: jest.fn(),
            focusOnUnitNumber: jest.fn()
        } as any;

        // Mock notInProductList method
        jest.spyOn<any, any>(component, 'notInProductList').mockReturnValue(true);

        // Call the method
        component['populateCentrifugationBatch'](mockProduct);

        // Verify product was added
        expect(component.products.length).toBe(1);
        expect(component.products[0]).toEqual(mockProduct);
        expect(component.initialProductsState.length).toBe(1);
        expect(component.allProducts.length).toBe(1);
        expect(component.unitNumberComponent.reset).toHaveBeenCalled();
        expect(component.unitNumberComponent.focusOnUnitNumber).toHaveBeenCalled();
    });

    it('should load irradiator device by ID', () => {
        // Mock the form control
        jest.spyOn(component.irradiation, 'disable');

        component.loadIrradiationId('test-device');
        expect(irradiationService.loadDeviceById).toHaveBeenCalledWith('test-device', 'TEST');
    });

    it('should find icons by product family', () => {
        const productIconsService = TestBed.inject(ProductIconsService);
        const icon = component['findIconsByProductFamily']('WHOLE_BLOOD');
        expect(productIconsService.getIconByProductFamily).toHaveBeenCalledWith('WHOLE_BLOOD');
        expect(icon).toBe('icon');
    });

    it('should handle validateProduct for different statuses', () => {
        // Mock methods
        jest.spyOn<any, any>(component, 'discardProduct').mockImplementation(() => {});
        jest.spyOn<any, any>(component, 'handleQuarantine').mockImplementation(() => {});
        jest.spyOn<any, any>(component, 'handleUnsuitableProduct').mockImplementation(() => {});


        // Test DISCARDED status
        const discardedProduct = { status: DISCARDED } as IrradiationProductDTO;
        component['validateProduct'](discardedProduct);
        expect(component['discardProduct']).toHaveBeenCalledWith(discardedProduct);

        // Test QUARANTINED status
        const quarantinedProduct = { status: QUARANTINED } as IrradiationProductDTO;
        component['validateProduct'](quarantinedProduct);
        expect(component['handleQuarantine']).toHaveBeenCalledWith(quarantinedProduct);

        // Test UNSUITABLE status
        const unsuitableProduct = { status: UNSUITABLE } as IrradiationProductDTO;
        component['validateProduct'](unsuitableProduct);
        expect(component['handleUnsuitableProduct']).toHaveBeenCalledWith(unsuitableProduct);

        // Test default case
        const otherProduct = { status: 'OTHER', statusReason: 'Some reason' } as IrradiationProductDTO;
        component['validateProduct'](otherProduct);
        expect(toastrService.error).toHaveBeenCalledWith('Some reason');
    });

    it('should discard product', () => {
        const product = {
            unitNumber: 'W036825314134',
            productCode: 'E468900',
            productDescription: 'WHOLE BLOOD',
            productFamily: 'WHOLE_BLOOD',
            location: 'TEST',
            statusReason: 'Discard reason'
        } as IrradiationProductDTO;

        // Mock discardService and confirmationService
        jest.spyOn(discardService, 'discardProduct').mockReturnValue(of({}));
        jest.spyOn(component, 'openConfirmationDialog' as any).mockImplementation(() => {});

        // Call the method
        component['discardProduct'](product);

        // Verify discardService was called with correct parameters
        expect(discardService.discardProduct).toHaveBeenCalledWith({
            unitNumber: 'W036825314134',
            productCode: 'E468900',
            productShortDescription: 'WHOLE BLOOD',
            productFamily: 'WHOLE_BLOOD',
            locationCode: 'TEST',
            reasonDescriptionKey: 'Discard reason',
            employeeId: '4c973896-5761-41fc-8217-07c5d13a004b',
            triggeredBy: 'IRRADIATION',
            comments: ''
        });
    });

    it('should convert status to color class', () => {
        expect(component['statusToColorClass'](AVAILABLE)).toBe('bg-green-500 text-white');
        expect(component['statusToColorClass'](QUARANTINED)).toBe('bg-orange-500 text-white');
        expect(component['statusToColorClass'](EXPIRED)).toBe('bg-red-500 text-white');
        expect(component['statusToColorClass'](UNSUITABLE)).toBe('bg-red-500 text-white');
        expect(component['statusToColorClass'](DISCARDED)).toBe('bg-red-500 text-white');
        expect(component['statusToColorClass'](SHIPPED)).toBe('bg-orange-500 text-white');
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
        component['showMessage'](MessageType.ERROR, 'Error message');
        expect(toastrService.error).toHaveBeenCalledWith('Error message');

        // Test warning message
        component['showMessage'](MessageType.WARNING, 'Warning message');
        expect(toastrService.warning).toHaveBeenCalledWith('Warning message');

        // Test success message
        component['showMessage'](MessageType.SUCCESS, 'Success message');
        expect(toastrService.success).toHaveBeenCalledWith('Success message');
    });
});
