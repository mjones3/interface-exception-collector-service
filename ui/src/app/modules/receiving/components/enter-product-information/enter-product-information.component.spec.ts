import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EnterProductInformationComponent } from './enter-product-information.component';
import { Field } from './enter-product-information.component';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { ProcessHeaderService } from '@shared';
import { Router, ActivatedRoute } from '@angular/router';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

describe('EnterProductInformationComponent', () => {
  let component: EnterProductInformationComponent;
  let fixture: ComponentFixture<EnterProductInformationComponent>;
  let toastrService: jest.Mocked<ToastrService>;
  let router: jest.Mocked<Router>;
  let headerService: jest.Mocked<ProcessHeaderService>;

  beforeEach(async () => {
    const toastrMock = {
      error: jest.fn(),
      success: jest.fn()
    };
    
    const routerMock = {
      navigateByUrl: jest.fn()
    };
    
    const headerServiceMock = {
      setTitle: jest.fn()
    };

    await TestBed.configureTestingModule({
      imports: [
        EnterProductInformationComponent,
        ReactiveFormsModule,
        BrowserAnimationsModule
      ],
      providers: [
        FormBuilder,
        { provide: ToastrService, useValue: toastrMock },
        { provide: Router, useValue: routerMock },
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
      expect(component.productInformationForm.get('bloodType')).toBeTruthy();
      expect(component.productInformationForm.get('expirationDate')).toBeTruthy();
      expect(component.productInformationForm.get('licenseStatus')).toBeTruthy();
      expect(component.productInformationForm.get('visualInspection')).toBeTruthy();
    });

    it('should have disabled fields initially', () => {
      expect(component.productInformationForm.get('productCode').disabled).toBeTruthy();
      expect(component.productInformationForm.get('bloodType').disabled).toBeTruthy();
      expect(component.productInformationForm.get('expirationDate').disabled).toBeTruthy();
    });
    it('should initialize form with empty values', () => {
      const controls = [
        'unitNumber',
        'productCode',
        'bloodType',
        'expirationDate',
        'licenseStatus',
        'visualInspection'
      ];

      controls.forEach(control => {
        expect(component.productInformationForm.get(control).value).toBe('');
      });
    });
  });

  describe('Barcode Scanning', () => {
    it('should handle barcode input when manual entry is not allowed', () => {
      component.manualEntryAllowed = false;
      const event = new KeyboardEvent('keydown', { key: '1' });
      
      jest.spyOn(Date.prototype, 'getTime').mockReturnValue(1000);
      component.lastKeyTime = 980; // Less than keyPressThreshold
      component.onKeyDown(Field.UNIT_NUMBER, event);
      
      expect(component.barcode).toContain('1');
    });

    it('should show error when manual entry is attempted', () => {
      component.manualEntryAllowed = false;
      const event = new KeyboardEvent('keydown', { key: '1' });
      
      jest.spyOn(Date.prototype, 'getTime').mockReturnValue(1100);
      component.lastKeyTime = 1000; // More than keyPressThreshold
      component.onKeyDown(Field.UNIT_NUMBER, event);
      
      expect(toastrService.error).toHaveBeenCalledWith('Manual Entry Not Allowed.');
    });
  });

  describe('Field Validation', () => {
    beforeEach(() => {
      // Initialize fieldsMap for tests
      component.fieldsMap.set(Field.UNIT_NUMBER, { focus: false });
      component.fieldsMap.set(Field.EXPIRATION_DATE, { focus: false });
    });

    it('should validate unit number format', () => {
      const control = Field.UNIT_NUMBER;
      component.productInformationForm.get(control).setValue('invalid');
      
      component.onTabEnter(control);
      
      expect(toastrService.error).toHaveBeenCalledWith('Unit Number is invalid.');
    });

    it('should validate expiration date format', () => {
      const control = Field.EXPIRATION_DATE;
      component.productInformationForm.get(control).setValue('invalid');
      
      component.onTabEnter(control);
      
      expect(toastrService.error).toHaveBeenCalledWith('Expiration Date is invalid.');
    });
  });

  describe('Product Code and Blood Type Validation', () => {
    beforeEach(() => {
      component.fieldsMap.set(Field.PRODUCT_CODE, { focus: false });
      component.fieldsMap.set(Field.BLOOD_TYPE, { focus: false });
    });

    it('should validate product code format', () => {
      const control = Field.PRODUCT_CODE;
      component.productInformationForm.get(control).setValue('invalid');
      
      component.onTabEnter(control);
      
      expect(toastrService.error).toHaveBeenCalledWith('Product Type is invalid.');
    });

    it('should validate blood type format', () => {
      const control = Field.BLOOD_TYPE;
      component.productInformationForm.get(control).setValue('invalid');
      
      component.onTabEnter(control);
      
      expect(toastrService.error).toHaveBeenCalledWith('Blood Type is invalid.');
    });

    it('should set focus on field after validation failure', () => {
      const control = Field.PRODUCT_CODE;
      component.productInformationForm.get(control).setValue('invalid');
      
      component.onTabEnter(control);
      
      expect(component.fieldsMap.get(control).focus).toBeTruthy();
    });
  });

  describe('Visual Inspection and License Status', () => {
    it('should return correct CSS class for visual inspection', () => {
      const result = component.getVisualInspectionClass('Satisfactory');
      expect(result).toBeDefined();
    });

    it('should return correct CSS class for license status', () => {
      const result = component.getLicenseStatusClass('Licensed');
      expect(result).toBeDefined();
    });

    it('should return correct CSS class for temperature product category', () => {
      const result = component.getTemperatureProductCategoryClass('Room Temperature');
      expect(result).toBeDefined();
    });

    it('should have correctly configured visual inspection options', () => {
      expect(component.visualInspectionOptions).toHaveLength(2);
      
      const satisfactory = component.visualInspectionOptions.find(opt => opt.value === 'Satisfactory');
      expect(satisfactory).toBeTruthy();
      expect(satisfactory.class).toBe('toggle-green');
      expect(satisfactory.iconName).toBe('hand-thumb-up');
      
      const unsatisfactory = component.visualInspectionOptions.find(opt => opt.value === 'Unsatisfactory');
      expect(unsatisfactory).toBeTruthy();
      expect(unsatisfactory.class).toBe('toggle-red');
      expect(unsatisfactory.iconName).toBe('hand-thumb-down');
    });

    it('should have correctly configured license options', () => {
      expect(component.licensedOptions).toHaveLength(2);
      
      const licensed = component.licensedOptions.find(opt => opt.value === 'Licensed');
      expect(licensed).toBeTruthy();
      expect(licensed.label).toBe('Licensed');
      
      const unlicensed = component.licensedOptions.find(opt => opt.value === 'Unlicensed');
      expect(unlicensed).toBeTruthy();
      expect(unlicensed.label).toBe('Unlicensed');
    });
  });

  describe('Table Configuration', () => {
    it('should have correct table configuration', () => {
      const config = component.importedProductsTableConfigComputed();
      
      expect(config.title).toBe('Added Products');
      expect(config.pageSize).toBe(20);
      expect(config.showPagination).toBeFalsy();
      expect(config.columns.length).toBe(8);
    });
  });

  describe('Paste Prevention', () => {
    it('should prevent paste when manual entry is not allowed', () => {
      component.manualEntryAllowed = false;
      
      component.onPaste(Field.UNIT_NUMBER);
      
      expect(toastrService.error).toHaveBeenCalledWith('Manual Entry Not Allowed.');
    });

    it('should allow paste when manual entry is allowed', () => {
      component.manualEntryAllowed = true;
      
      component.onPaste(Field.UNIT_NUMBER);
      
      expect(toastrService.error).not.toHaveBeenCalled();
    });
  });
});