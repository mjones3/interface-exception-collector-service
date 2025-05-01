import { CommonModule, DatePipe } from '@angular/common';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { MatIconTestingModule } from '@angular/material/icon/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { ProcessHeaderService, ToastrImplService } from '@shared';
import { MockStore, provideMockStore } from '@ngrx/store/testing';
import { ApolloTestingModule } from 'apollo-angular/testing';
import { ScanUnitNumberProductCodeComponent } from 'app/shared/components/scan-unit-number-product-code/scan-unit-number-product-code.component';
import { ProductIconsService } from 'app/shared/services/product-icon.service';
import { CookieService } from 'ngx-cookie-service';
import { ToastrModule } from 'ngx-toastr';
import { of } from 'rxjs';
import { AddRecoveredPlasmaProductsComponent } from './add-recovered-plasma-products.component';
import { RecoveredPlasmaService } from '../../services/recovered-plasma.service';
import { CartonDTO, CartonPackedItemResponseDTO } from '../../models/recovered-plasma.dto';

describe('AddRecoveredPlasmaProductsComponent', () => {
    let component: AddRecoveredPlasmaProductsComponent;
    let fixture: ComponentFixture<AddRecoveredPlasmaProductsComponent>;
    let mockStore: MockStore;
    let mockRouter: jest.Mocked<Router>;
    let mockRecoveredPlasmaService: jest.Mocked<RecoveredPlasmaService>;
    let mockToastrService: jest.Mocked<ToastrImplService>;
    let mockProductIconsService: jest.Mocked<ProductIconsService>;
    let mockCookieService: jest.Mocked<CookieService>;
    let mockProcessHeaderService: jest.Mocked<ProcessHeaderService>;
    let mockScanUnitNumberProductCode: jest.Mocked<ScanUnitNumberProductCodeComponent>;

    const mockCartonDetails: CartonDTO = {
        id: 1,
        cartonNumber: 'CART123',
        shipmentId: 100,
        maxNumberOfProducts: 5,
        minNumberOfProducts: 1,
        packedProducts: [
            {
                id: 1,
                unitNumber: 'W121212121221',
                productCode: 'EPROD001',
                productType: 'PLASMA'
            } as CartonPackedItemResponseDTO,
            {
                id: 2,
                unitNumber: 'W121212121223',
                productCode: 'EPROD002',
                productType: 'PLASMA'
            } as CartonPackedItemResponseDTO
        ]
    };

    beforeEach(async () => {
        // Create Jest mocks for all dependencies
        mockRouter = {navigate: jest.fn()} as Partial<Router> as jest.Mocked<Router>;
        
        mockRecoveredPlasmaService = {
            getShipmentById: jest.fn()
        } as Partial<RecoveredPlasmaService> as jest.Mocked<RecoveredPlasmaService>;        
        mockToastrService = {
            success: jest.fn(),
            error: jest.fn()
        } as Partial<ToastrImplService> as jest.Mocked<ToastrImplService>;
        
        mockProductIconsService = {
            getIconByProductFamily: jest.fn().mockReturnValue('plasma-icon')
        } as Partial<ProductIconsService> as jest.Mocked<ProductIconsService>;
        
        mockCookieService = {
            get: jest.fn()
        } as Partial<CookieService> as jest.Mocked<CookieService>;
        
        mockProcessHeaderService = {
            setTitle: jest.fn()
        } as Partial<ProcessHeaderService> as jest.Mocked<ProcessHeaderService>;
        
        mockScanUnitNumberProductCode = {
            disableUnitProductGroup: jest.fn(),
            resetUnitProductGroup: jest.fn(),
            focusOnUnitNumber: jest.fn()
        } as Partial<ScanUnitNumberProductCodeComponent> as jest.Mocked<ScanUnitNumberProductCodeComponent>;

        await TestBed.configureTestingModule({
            imports: [
                AddRecoveredPlasmaProductsComponent,
                MatIconTestingModule,
                NoopAnimationsModule,
                CommonModule,
                ApolloTestingModule,
                ToastrModule.forRoot(),
            ],
            providers: [
                FormBuilder,
                DatePipe,
                provideMockStore({
                    initialState: {
                        auth: {
                            id: 'EMP123'
                        }
                    }
                }),
                { provide: Router, useValue: mockRouter },
                { provide: RecoveredPlasmaService, useValue: mockRecoveredPlasmaService },
                { provide: ToastrImplService, useValue: mockToastrService },
                { provide: ProductIconsService, useValue: mockProductIconsService },
                { provide: CookieService, useValue: mockCookieService },
                { provide: ProcessHeaderService, useValue: mockProcessHeaderService },
                {
                    provide: ActivatedRoute,
                    useValue: {
                        snapshot: {
                            params: {
                                id: '100'
                            }
                        },
                        paramMap: of({})
                    },
                },
            ],
        }).compileComponents();

        mockStore = TestBed.inject(MockStore);
        fixture = TestBed.createComponent(AddRecoveredPlasmaProductsComponent);
        component = fixture.componentInstance;
        component.cartonDetails.set(mockCartonDetails);
        component.scanUnitNumberProductCode = mockScanUnitNumberProductCode;
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should initialize and call disableInputsIfMaxCartonProduct on ngOnInit', () => {
        jest.spyOn(component, 'disableInputsIfMaxCartonProduct');
        component.ngOnInit();
        expect(component.disableInputsIfMaxCartonProduct).toHaveBeenCalled();
    });

    it('should emit unitNumberProductCode when addProduct is called', () => {
        jest.spyOn(component.unitNumberProductCode, 'emit');
        const mockEvent = { unitNumber: 'W121212123231', productCode: 'EPROD003' };
        component.addProduct(mockEvent);
        expect(component.unitNumberProductCode.emit).toHaveBeenCalledWith(mockEvent);
    });

    it('should return packed products from cartonDetails', () => {
        const result = component.getPackedProducts();
        expect(result).toEqual(mockCartonDetails.packedProducts);
    });

    it('should return empty array if packedProducts is undefined', () => {
        component.cartonDetails.set({ ...mockCartonDetails, packedProducts: undefined });
        const result = component.getPackedProducts();
        expect(result).toEqual([]);
    });

    it('should disable inputs if max carton products reached', () => {
        component.cartonDetails.set({
            ...mockCartonDetails,
            packedProducts: Array(5).fill({} as CartonPackedItemResponseDTO),
            maxNumberOfProducts: 5
        });
        component.disableInputsIfMaxCartonProduct();
        expect(mockScanUnitNumberProductCode.disableUnitProductGroup).toHaveBeenCalled();
    });

    it('should not disable inputs if max carton products not reached', () => {
        component.cartonDetails.set({
            ...mockCartonDetails,
            packedProducts: Array(3).fill({} as CartonPackedItemResponseDTO),
            maxNumberOfProducts: 5
        });
        
        component.disableInputsIfMaxCartonProduct();
        expect(mockScanUnitNumberProductCode.disableUnitProductGroup).not.toHaveBeenCalled();
    });

    it('should call disableUnitProductGroup when disableProductGroup is called', () => {
        component.disableProductGroup();
        expect(mockScanUnitNumberProductCode.disableUnitProductGroup).toHaveBeenCalled();
    });

    it('should call resetUnitProductGroup when resetProductGroup is called', () => {
        component.resetProductGroup();
        expect(mockScanUnitNumberProductCode.resetUnitProductGroup).toHaveBeenCalled();
    });

    it('should call focusOnUnitNumber when focusOnUnitNumber is called', () => {
        component.focusOnUnitNumber();
        expect(mockScanUnitNumberProductCode.focusOnUnitNumber).toHaveBeenCalled();
    });

    it('should compute maxProducts correctly', () => {
        const maxProducts = component.maxProductsComputed();
        expect(maxProducts).toBe(mockCartonDetails.maxNumberOfProducts);
    });

    it('should compute minProducts correctly', () => {
        const minProducts = component.minProductsComputed();
        expect(minProducts).toBe(mockCartonDetails.minNumberOfProducts);
    });
        
    it('should handle null cartonDetails in getPackedProducts', () => {
        component.cartonDetails.set(null);
        const result = component.getPackedProducts();
        expect(result).toEqual([]);
    });   
});