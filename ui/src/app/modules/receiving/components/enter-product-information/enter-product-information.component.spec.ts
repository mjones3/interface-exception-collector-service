import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EnterProductInformationComponent } from './enter-product-information.component';
import { Field } from './enter-product-information.component';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { ProcessHeaderService, ToastrImplService } from '@shared';
import { Router, ActivatedRoute } from '@angular/router';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ReceivingService } from '../../service/receiving.service';
import { AuthState } from 'app/core/state/auth/auth.reducer';
import { provideMockStore } from '@ngrx/store/testing';
import { of, throwError } from 'rxjs';
import { ApolloError } from '@apollo/client/errors';
import { MutationResult } from 'apollo-angular';
import { ApolloTestingModule } from 'apollo-angular/testing';
import { QueryResult } from '@apollo/client';

describe('EnterProductInformationComponent', () => {
  let component: EnterProductInformationComponent;
  let fixture: ComponentFixture<EnterProductInformationComponent>;
  let toastrService: jest.Mocked<ToastrService | ToastrImplService>;
  let router: jest.Mocked<Router>;
  let headerService: jest.Mocked<ProcessHeaderService>;
  let mockReceivingService: jest.Mocked<ReceivingService>

  const initialState: AuthState = {
    id: 'mock-user-id',
    loaded: true,
  };

  beforeEach(async () => {
    const toastrMock = {
      error: jest.fn(),
      success: jest.fn(),
      warning: jest.fn(),
      info: jest.fn(),
      show: jest.fn()
    };
    
    const routerMock = {
      navigateByUrl: jest.fn()
    };

    mockReceivingService = {
      validateScannedField: jest.fn().mockReturnValue(of()),
      getImportById: jest.fn().mockReturnValue(of()),
      addImportItems: jest.fn().mockReturnValue(of()),
      completeImport: jest.fn().mockReturnValue(of())
    } as Partial<ReceivingService> as jest.Mocked<ReceivingService>;

    const headerServiceMock = {
      setTitle: jest.fn()
    };

    await TestBed.configureTestingModule({
      imports: [
        EnterProductInformationComponent,
        ReactiveFormsModule,
        ApolloTestingModule,
        BrowserAnimationsModule
      ],
      providers: [
        FormBuilder,
        provideMockStore({ initialState}),
        { provide: ToastrService, useValue: toastrMock },
        { provide: Router, useValue: routerMock },
        { provide: ReceivingService, useValue: mockReceivingService },
        { provide: ProcessHeaderService, useValue: headerServiceMock },
        { provide: ActivatedRoute, useValue: {} }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(EnterProductInformationComponent);
    component = fixture.componentInstance;
    toastrService = TestBed.inject(ToastrService) as jest.Mocked<ToastrService>;
    router = TestBed.inject(Router) as jest.Mocked<Router>;
    headerService = TestBed.inject(ProcessHeaderService) as jest.Mocked<ProcessHeaderService>;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Form Initialization', () => {
    it('should initialize the form with required fields', () => {
      expect(component.productInformationForm.get('unitNumber')).toBeTruthy();
      expect(component.productInformationForm.get('productCode')).toBeTruthy();
      expect(component.productInformationForm.get('aboRh')).toBeTruthy();
      expect(component.productInformationForm.get('expirationDate')).toBeTruthy();
      expect(component.productInformationForm.get('licenseStatus')).toBeTruthy();
      expect(component.productInformationForm.get('visualInspection')).toBeTruthy();
    });

    it('should have disabled fields initially', () => {
      expect(component.productInformationForm.get('productCode').disabled).toBeTruthy();
      expect(component.productInformationForm.get('aboRh').disabled).toBeTruthy();
      expect(component.productInformationForm.get('expirationDate').disabled).toBeTruthy();
    });
    it('should initialize form with empty values', () => {
      const controls = [
        'unitNumber',
        'productCode',
        'aboRh',
        'expirationDate',
        'licenseStatus',
        'visualInspection'
      ];

      controls.forEach(control => {
        expect(component.productInformationForm.get(control).value).toBe('');
      });
    });
  });


  describe('Visual Inspection and License Status', () => {
    it('should return correct CSS class for visual inspection', () => {
      const result = component.getVisualInspectionClass('SATISFACTORY');
      expect(result).toBeDefined();
    });

    it('should return correct CSS class for license status', () => {
      const result = component.getLicenseStatusClass('LICENSED');
      expect(result).toBeDefined();
    });

    it('should return correct CSS class for temperature product category', () => {
      const result = component.getTemperatureProductCategoryClass('ROOM_TEMPERATURE');
      expect(result).toBeDefined();
    });

    it('should have correctly configured visual inspection options', () => {
      expect(component.visualInspectionOptions).toHaveLength(2);
      
      const satisfactory = component.visualInspectionOptions.find(opt => opt.value === 'SATISFACTORY');
      expect(satisfactory).toBeTruthy();
      expect(satisfactory.class).toBe('toggle-green');
      expect(satisfactory.iconName).toBe('hand-thumb-up');
      
      const unsatisfactory = component.visualInspectionOptions.find(opt => opt.value === 'UNSATISFACTORY');
      expect(unsatisfactory).toBeTruthy();
      expect(unsatisfactory.class).toBe('toggle-red');
      expect(unsatisfactory.iconName).toBe('hand-thumb-down');
    });

    it('should have correctly configured license options', () => {
      expect(component.licenseOptions).toHaveLength(2);
      
      const licensed = component.licenseOptions.find(opt => opt.value === 'LICENSED');
      expect(licensed).toBeTruthy();
      
      const unlicensed = component.licenseOptions.find(opt => opt.value === 'UNLICENSED');
      expect(unlicensed).toBeTruthy();
    });
  });

  describe('Form Validation', () => {
    it('should validate fields correctly', () => {
      expect(component.isFormValid()).toBeFalsy();

      const fieldValidatedWithValues = [
        Field.UNIT_NUMBER,
        Field.ABO_RH,
        Field.PRODUCT_CODE,
        Field.EXPIRATION_DATE
      ];

      fieldValidatedWithValues.forEach(field => {
        const control = component.productInformationForm.get(field);
        control.setValue(`=${field}`);
        control.disable();
      });

      component.productInformationForm.patchValue({
        licenseStatus: 'LICENSED',
        visualInspection: 'UNSATISFACTORY'
      });

      expect(component.isFormValid()).toBeTruthy();
    });

    it('should require backend fields to be disabled and have values', () => {
      const fieldValidatedWithValues = [
        Field.UNIT_NUMBER,
        Field.ABO_RH,
        Field.PRODUCT_CODE,
        Field.EXPIRATION_DATE
      ];

      fieldValidatedWithValues.forEach(field => {
        const control = component.productInformationForm.get(field);
        control.setValue(`=${field}`); 
      });

      component.productInformationForm.patchValue({
        licenseStatus: 'LICENSED',
        visualInspection: 'SATISFACTORY'
      });

      expect(component.isFormValid()).toBeFalsy();
    });

    it('should validate all required fields have validators', () => {
      const requiredFields = [
        Field.UNIT_NUMBER,
        Field.ABO_RH,
        Field.PRODUCT_CODE,
        Field.EXPIRATION_DATE,
        Field.LICENSE_STATUS,
        Field.VISUAL_INSPECTION
      ];

      // Check each field is required
      requiredFields.forEach(field => {
        const control = component.productInformationForm.get(field);
        expect(control.hasValidator(Validators.required)).toBeTruthy();
      });
    });
  });

  describe('Table Configuration', () => {
    it('should have correct table configuration', () => {
      const config = component.importItemsTableConfigComputed();
      
      expect(config.title).toBe('Added Products');
      expect(config.showPagination).toBeFalsy();
      expect(config.columns.length).toBe(8);
    });
  });

  describe('Form Reset', () => {
    it('should reset form to initial state', () => {
      component.productInformationForm.patchValue({
        unitNumber: '=123',
        productCode: '=456',
        aboRh: '=AB+',
        expirationDate: '&2024-12-31',
        licenseStatus: 'LICENSED',
        visualInspection: 'SATISFACTORY'
      });

      // Disable some fields to simulate form in use
      component.productInformationForm.get('unitNumber').disable();
      component.productInformationForm.get('productCode').disable();
      component.productInformationForm.get('aboRh').disable();
      component.productInformationForm.get('expirationDate').disable();

      component.resetForm();

      // Verify field states match initial configuration
      expect(component.productInformationForm.get('unitNumber').enabled).toBeTruthy();
      expect(component.productInformationForm.get('productCode').disabled).toBeTruthy();
      expect(component.productInformationForm.get('aboRh').disabled).toBeTruthy();
      expect(component.productInformationForm.get('expirationDate').disabled).toBeTruthy();
    });
  });

  describe('Tab Enter Behavior', () => {
    it('should validate and process valid unit number', () => {
      const mockValidResponse = {
        data: {
          validateBarcode: {
            data: {
              valid: true,
              result: 'W12121212101',
              resultDescription: null
            },
            notifications: []
          }
        }
      } as QueryResult;
      mockReceivingService.validateScannedField.mockReturnValue(of(mockValidResponse));
      
      // Set initial form state
      component.productInformationForm.get('unitNumber').setValue('=W1212121210100');
      component.productInformationForm.get('aboRh').disable();
      
      component.onTabEnter(Field.UNIT_NUMBER);

      expect(mockReceivingService.validateScannedField).toHaveBeenCalledWith({
        temperatureCategory: undefined,
        barcodeValue: '=W1212121210100',
        barcodePattern: 'BARCODE_UNIT_NUMBER'
      });
      
      // Verify field state changes
      expect(component.productInformationForm.get('unitNumber').value).toBe(mockValidResponse.data.validateBarcode.data.result);
      expect(component.productInformationForm.get('unitNumber').disabled).toBeTruthy();
      expect(component.productInformationForm.get('aboRh').enabled).toBeTruthy();
    });

    it('should handle invalid barcode format', () => {
      component.productInformationForm.get('unitNumber').setValue('INVALID');
      component.fieldsMap.get(Field.UNIT_NUMBER).focus = false;
      
      component.onTabEnter(Field.UNIT_NUMBER);
      
      expect(component.fieldsMap.get(Field.UNIT_NUMBER).focus).toBeTruthy();
      expect(mockReceivingService.validateScannedField).not.toHaveBeenCalled();
    });

    it('should handle Apollo error', () => {
      const apolloError = new ApolloError({
        errorMessage: 'Network error'
      });
      mockReceivingService.validateScannedField.mockReturnValue(throwError(() => apolloError));
      
      component.productInformationForm.get('unitNumber').setValue('=TEST123');
      component.onTabEnter(Field.UNIT_NUMBER);

      expect(mockReceivingService.validateScannedField).toHaveBeenCalled();
      expect(toastrService.error).toHaveBeenCalled();
    });
  });

  describe('Add Import Items', () => {
    const mockSuccessResponse = {
      data: {
        createImportItem: {
          data: {
            id: 1,
            products: [{
              id: 1,
              unitNumber: 'W121212121212',
              productCode: 'E2323232',
              aboRh: 'AB+',
              expirationDate: '2024-12-31',
              licenseStatus: 'LICENSED',
              visualInspection: 'SATISFACTORY',
              employeeId: 'mock-user-id',
              importId: 1
            }]
          },
          notifications: [{
            type: 'SUCCESS',
            message: 'Item added successfully'
          }]
        }
      }
    } as MutationResult;

    beforeEach(() => {
      component.employeeId = 'mock-user-id';
      //set value for importData
      component.importData.set({
        id: 1,
        maxNumberOfProducts: 11,
        temperatureCategory: '',
        transitStartDateTime:'',
        transitStartTimeZone:'',
        transitEndDateTime:'',
        transitEndTimeZone:'',
        temperature:null,
        thermometerCode:'',
        locationCode:'',
        isQuarantined: true,
        comments:'',
        employeeId:'',
        products: []
      });
    });

  
    describe('addImportItems', () => {
      it('should successfully add import items', () => {
        mockReceivingService.addImportItems.mockReturnValue(of(mockSuccessResponse));
        const mockAddReq = jest.spyOn(component, 'prepareAddProductReq');
        component.productInformationForm.patchValue({
          unitNumber: '123',
          productCode: '456',
          aboRh: 'AB+',
          expirationDate: '2024-12-31',
          licenseStatus: 'LICENSED',
          visualInspection: 'SATISFACTORY'
        });
        component.addImportItemRequest.set({
          unitNumber: '123',
          productCode: '456',
          aboRh: 'AB+',
          expirationDate: '2024-12-31',
          licenseStatus: '',
          visualInspection: '',
          employeeId: '',
          importId: null
        })
        component.addImportItems();
        expect(mockAddReq).toHaveBeenCalled()
        expect(mockReceivingService.addImportItems).toHaveBeenCalledWith({
          unitNumber: '123',
          productCode: '456',
          aboRh: 'AB+',
          expirationDate: '2024-12-31',
          licenseStatus: 'LICENSED',
          visualInspection: 'SATISFACTORY',
          employeeId: 'mock-user-id',
          importId: 1
        });
        expect(component.importData()).toEqual(mockSuccessResponse.data.createImportItem.data);
      });

      it('should handle duplicate product error', () => {
        const duplicateError = new ApolloError({
          errorMessage: 'R2DBC commit'
        });
        mockReceivingService.addImportItems.mockReturnValue(throwError(() => duplicateError));
        const formData = {
          unitNumber: '=123',
          productCode: '=456',
          aboRh: '=AB+',
          expirationDate: '&2024-12-31',
          licenseStatus: 'LICENSED',
          visualInspection: 'SATISFACTORY'
        };
        
        component.productInformationForm.patchValue(formData);
        component.addImportItems();
        expect(toastrService.show).toHaveBeenCalledWith('Product already added', null, {}, 'error');
      });

      it('should handle general Apollo error and preserve form state', () => {
        const generalError = new ApolloError({
          errorMessage: 'Network error'
        });
        mockReceivingService.addImportItems.mockReturnValue(throwError(() => generalError));
        
        const formData = {
          unitNumber: '=123',
          productCode: '=456',
          aboRh: '=AB+',
          expirationDate: '&2024-12-31',
          licenseStatus: 'LICENSED',
          visualInspection: 'SATISFACTORY'
        };
        
        component.productInformationForm.patchValue(formData);
        component.addImportItems();
        expect(mockReceivingService.addImportItems).toHaveBeenCalled();
        expect(toastrService.error).toHaveBeenCalled();
        
        // Verify form state was preserved
        Object.entries(formData).forEach(([key, value]) => {
          expect(component.productInformationForm.get(key).value).toBe(value);
        });
      });

      it('should handle success response with notifications', () => {
        const responseWithNotifications = {
          data: {
            createImportItem: {
              data: {
                id: 1,
                products: []
              },
              notifications: [
                { type: 'SUCCESS', message: 'Item added successfully' }
              ]
            }
          }
        } as MutationResult;

        mockReceivingService.addImportItems.mockReturnValue(of(responseWithNotifications));
        
        component.addImportItems();

        expect(mockReceivingService.addImportItems).toHaveBeenCalled();
        expect(component.importData()).toEqual(responseWithNotifications.data.createImportItem.data);
      });
    });
  });

  describe('Field Mapping and Initialization', () => {
    it('should initialize parse type map correctly', () => {
      expect(component.parseTypeMap.get('unitNumber')).toBe('BARCODE_UNIT_NUMBER');
      expect(component.parseTypeMap.get('productCode')).toBe('BARCODE_PRODUCT_CODE');
      expect(component.parseTypeMap.get('expirationDate')).toBe('BARCODE_EXPIRATION_DATE');
      expect(component.parseTypeMap.get('aboRh')).toBe('BARCODE_BLOOD_GROUP');
    });

    it('should initialize fields map correctly', () => {
      expect(component.fieldsMap.get(Field.UNIT_NUMBER).focus).toBeTruthy();
      expect(component.fieldsMap.get(Field.ABO_RH).focus).toBeFalsy();
      expect(component.fieldsMap.get(Field.PRODUCT_CODE).focus).toBeFalsy();
      expect(component.fieldsMap.get(Field.EXPIRATION_DATE).focus).toBeFalsy();
    });

    it('should have correct next control mapping', () => {
      expect(component.nextControlMap[Field.UNIT_NUMBER]).toEqual([Field.ABO_RH]);
      expect(component.nextControlMap[Field.ABO_RH]).toEqual([Field.PRODUCT_CODE]);
      expect(component.nextControlMap[Field.PRODUCT_CODE]).toEqual([Field.EXPIRATION_DATE]);
    });

    it('should initialize with correct field display names', () => {
      expect(component.fieldDisplayNames[Field.UNIT_NUMBER]).toBe('Unit Number');
      expect(component.fieldDisplayNames[Field.PRODUCT_CODE]).toBe('Product Code');
      expect(component.fieldDisplayNames[Field.EXPIRATION_DATE]).toBe('Expiration Date');
      expect(component.fieldDisplayNames[Field.ABO_RH]).toBe('ABO/RH');
      expect(component.fieldDisplayNames[Field.VISUAL_INSPECTION]).toBe('Visual Inspection');
      expect(component.fieldDisplayNames[Field.LICENSE_STATUS]).toBe('License Status');
    });
  });


  describe('completeImport', () => {
    const mockImportId = 123;
    const mockEmployeeId = 'mock-user-id';
    const mockNextUrl = '/next-page';

    beforeEach(() => {
      component.employeeId = 'mock-user-id';
      //set value for importData
      const mockData = component.importData.set({
        id: 1,
        maxNumberOfProducts: 11,
        temperatureCategory: '',
        transitStartDateTime:'',
        transitStartTimeZone:'',
        transitEndDateTime:'',
        transitEndTimeZone:'',
        temperature:null,
        thermometerCode:'',
        locationCode:'',
        isQuarantined: true,
        comments:'',
        employeeId:'',
        products: []
      });
    })

    it('should prepare correct request payload', () => {
      const request = component.completeRequest();
      expect(request).toEqual({
        importId: 1,
        completeEmployeeId: mockEmployeeId
      });
    });

    it('should call service and handle successful response', () => {
      const mockResponse = {
        data: {
          completeImport: {
            _links: {
              next: '/next-page'
            },
            notifications: [
              { type: 'SUCCESS', message: 'Import completed' }
            ]
          }
        }
      } as MutationResult

      jest.spyOn(mockReceivingService,'completeImport').mockReturnValue(of(mockResponse));
      jest.spyOn(router, 'navigateByUrl');

      jest.spyOn(component, 'handleNavigation');

      component.completeImport();
      expect(mockReceivingService.completeImport).toHaveBeenCalledWith({
        importId: 1,
        completeEmployeeId: mockEmployeeId
      });
      expect(router.navigateByUrl).toHaveBeenCalled();
      expect(component.handleNavigation).toHaveBeenCalled();
    });

    it('should handle error response', () => {
      const error = new ApolloError({ graphQLErrors: [{ message: 'Test error' }] });
      jest.spyOn(toastrService, 'error');
      jest.spyOn(mockReceivingService,'completeImport').mockReturnValue(throwError(() => error));
      component.completeImport();

      expect(mockReceivingService.completeImport).toHaveBeenCalledWith({
        importId: 1,
        completeEmployeeId: mockEmployeeId
      });
      expect(toastrService.error).toHaveBeenCalled();
    });

    it('should not navigate if no next URL is provided', () => {
      const mockResponse = {
        data: {
          completeImport: {
            notifications: [
              { type: 'SUCCESS', message: 'Import completed' }
            ]
          }
        }
      };

      mockReceivingService.completeImport.mockReturnValue(of(mockResponse));
      jest.spyOn(component, 'handleNavigation');
      component.completeImport();

      expect(component.handleNavigation).not.toHaveBeenCalled();
    });
  });
});