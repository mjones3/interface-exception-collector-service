import { CommonModule, DatePipe } from '@angular/common';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { MatIconTestingModule } from '@angular/material/icon/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { ProcessHeaderService } from '@shared';
import { MockStore, provideMockStore } from '@ngrx/store/testing';
import { ApolloTestingModule } from 'apollo-angular/testing';
import { ScanUnitNumberProductCodeComponent } from 'app/shared/components/scan-unit-number-product-code/scan-unit-number-product-code.component';
import { ProductIconsService } from 'app/shared/services/product-icon.service';
import { CookieService } from 'ngx-cookie-service';
import { ToastrModule, ToastrService } from 'ngx-toastr';
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
    let mockToastrService: jest.Mocked<ToastrService>;
    let mockProductIconsService: jest.Mocked<ProductIconsService>;
    let mockCookieService: jest.Mocked<CookieService>;
    let mockProcessHeaderService: jest.Mocked<ProcessHeaderService>;
    let mockScanUnitNumberProductCode: jest.Mocked<ScanUnitNumberProductCodeComponent>;

    // Mock CartonDTO for testing
    const mockCartonData: CartonDTO = {
        id: 3,
        cartonNumber: "BPMMH13",
        shipmentId: 2,
        cartonSequence: 1,
        createEmployeeId: "4c973896-5761-41fc-8217-07c5d13a004b",
        closeEmployeeId: null,
        createDate: "2025-05-01T19:35:06.663415Z",
        modificationDate: "2025-05-01T19:35:06.663415Z",
        closeDate: null,
        status: "OPEN",
        totalProducts: 0,
        totalWeight: 0,
        totalVolume: 0,
        packedProducts: [],
        maxNumberOfProducts: 20,
        minNumberOfProducts: 1,
        canVerify: false,
        canClose: false,
        verifiedProducts: [],
        failedCartonItem: null
    };

    // Mock packed products data
    const mockPackedProductData: CartonPackedItemResponseDTO = {
        id: 5,
        cartonId: "3",
        unitNumber: "W036898786801",
        productCode: "E2534V00",
        productDescription: "CPD PLS MI 24H",
        productType: "RP_FROZEN_WITHIN_24_HOURS",
        volume: 259,
        weight: 150,
        packedByEmployeeId: "4c973896-5761-41fc-8217-07c5d13a004b",
        aboRh: "AP",
        status: "PACKED",
        expirationDate: "2024-09-03T10:15:30",
        collectionDate: "2011-12-03T09:15:30Z",
        createDate: "2025-05-01T22:03:13.598779Z",
        modificationDate: "2025-05-01T22:03:13.598779Z",
        verifiedByEmployeeId: null,
        verifyDate: null
    };

    beforeEach(async () => {
        // Create mocks for all dependencies
        mockRouter = { navigate: jest.fn() } as Partial<Router> as jest.Mocked<Router>;
        
        mockRecoveredPlasmaService = {
            getShipmentById: jest.fn()
        } as Partial<RecoveredPlasmaService> as jest.Mocked<RecoveredPlasmaService>;        
        
        mockToastrService = {
            success: jest.fn(),
            error: jest.fn()
        } as Partial<ToastrService> as jest.Mocked<ToastrService>;
        
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
                { provide: ToastrService, useValue: mockToastrService },
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
        
        // Set up the input signal with mock data
        Object.defineProperty(component, 'cartonDetails', {
            get: () => {
                return () => mockCartonData;
            }
        });
        
        component.scanUnitNumberProductCode = mockScanUnitNumberProductCode;
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should emit unitNumberProductCode when addProduct is called', () => {
        jest.spyOn(component.unitNumberProductCode, 'emit');
        const mockEvent = { unitNumber: 'W121212123231', productCode: 'EPROD003' };
        component.addProduct(mockEvent);
        expect(component.unitNumberProductCode.emit).toHaveBeenCalledWith(mockEvent);
    });

    it('should return packed products from cartonDetails', () => {
        const result = component.getPackedProducts();
        expect(result).toEqual(mockCartonData.packedProducts);
    });

    it('should return empty array if packedProducts is undefined', () => {
        const mockCartonWithUndefinedProducts = { ...mockCartonData, packedProducts: undefined };
        Object.defineProperty(component, 'cartonDetails', {
            get: () => {
                return () => mockCartonWithUndefinedProducts;
            }
        });
        const result = component.getPackedProducts();
        expect(result).toEqual([]);
    });

    it('should disable inputs if max carton products reached', () => {
        const mockCartonWithMaxProducts = { 
            ...mockCartonData, 
            packedProducts: Array(20).fill(mockPackedProductData),
            maxNumberOfProducts: 20
        };
        component.disableInputsIfMaxCartonProduct(mockCartonWithMaxProducts);
        expect(mockScanUnitNumberProductCode.disableUnitProductGroup).toHaveBeenCalled();
    });

    it('should not disable inputs if max carton products not reached', () => {
        const mockCartonWithFewProducts = { 
            ...mockCartonData, 
            packedProducts: Array(3).fill(mockPackedProductData),
            maxNumberOfProducts: 5
        };
        
        Object.defineProperty(component, 'cartonDetails', {
            get: () => {
                return () => mockCartonWithFewProducts;
            }
        });
        
        component.disableInputsIfMaxCartonProduct(mockCartonData);
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
        expect(maxProducts).toBe(mockCartonData.maxNumberOfProducts);
    });

    it('should compute minProducts correctly', () => {
        const minProducts = component.minProductsComputed();
        expect(minProducts).toBe(mockCartonData.minNumberOfProducts);
    });
        
    it('should handle null cartonDetails in getPackedProducts', () => {
        const mockCartonWithNullProducts = { ...mockCartonData, packedProducts: null };
        Object.defineProperty(component, 'cartonDetails', {
            get: () => {
                return () => mockCartonWithNullProducts;
            }
        });
        const result = component.getPackedProducts();
        expect(result).toEqual([]);
    });
    
    it('should properly handle cartonDetails input property', () => {
        expect(component.cartonDetails()).toBeDefined();
        expect(component.cartonDetails().id).toBe(3);
        expect(component.cartonDetails().cartonNumber).toBe('BPMMH13');
    });
    
    it('should update computed values when cartonDetails changes', () => {
        const updatedMockCarton = { 
            ...mockCartonData, 
            maxNumberOfProducts: 30,
            minNumberOfProducts: 5
        };
        Object.defineProperty(component, 'cartonDetails', {
            get: () => {
                return () => updatedMockCarton;
            }
        });
        expect(component.maxProductsComputed()).toBe(30);
        expect(component.minProductsComputed()).toBe(5);
    });
});