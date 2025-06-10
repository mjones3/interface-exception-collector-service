import { AsyncPipe, DatePipe, NgTemplateOutlet, UpperCasePipe } from '@angular/common';
import { AfterViewInit, Component, computed, OnInit, signal, TemplateRef, viewChild, ElementRef } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonToggleGroup } from '@angular/material/button-toggle';
import { MatDialogActions } from '@angular/material/dialog';
import { MatFormField, MatLabel } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelect } from '@angular/material/select';
import { ActivatedRoute, Router } from '@angular/router';
import { NotificationTypeMap, ProcessHeaderComponent, ProcessHeaderService, TableConfiguration } from '@shared';
import { ActionButtonComponent } from 'app/shared/components/buttons/action-button.component';
import { BasicButtonComponent } from 'app/shared/components/buttons/basic-button.component';
import { CustomButtonToggleComponent } from 'app/shared/components/custom-button-toggle/custom-button-toggle.component';
import { InputComponent } from 'app/shared/components/input/input.component';
import { TableComponent } from 'app/shared/components/table/table.component';
import { ButtonOption } from 'app/shared/models/custom-button-toggle.model';
import { ToastrService } from 'ngx-toastr';
import { ImportedProductInformationDTO } from '../../models/product-information.dto';
import { MatIcon } from '@angular/material/icon';
import { licenseStatusCssMap, quarantinedCssMap, quarantinedValueMap, temperatureProductCategoryCssMap, visualInspectionCssMap } from '../../graphql/query-definitions/imports-enter-shipping-information.graphql';
import { snakeCase } from 'lodash';
import { FuseCardComponent } from '@fuse/components/card/public-api';
import { scannedValidatorStartWithAnd, scannedValidatorStartWithEqual } from 'app/shared/forms/biopro-validators';
import { map } from 'rxjs';
import { ReceivingService } from '../../service/receiving.service';
import { ValidateBarcodeRequestDTO } from '../../graphql/query-definitions/validate-bar-code.graphql';
import { consumeUseCaseNotifications } from 'app/shared/utils/notification.handling';
 
export enum Field {
  UNIT_NUMBER = 'unitNumber',
  PRODUCT_CODE = 'productCode',
  EXPIRATION_DATE = 'expirationDate',
  ABO_RH = 'aboRh',
  VISUAL_INSPECTION = 'visualInspection',
  LICENSE_STATUS = 'licenseStatus'
}

@Component({
  selector: 'biopro-enter-product-information',
  standalone: true,
  imports: [
    ActionButtonComponent,
    AsyncPipe,
    FuseCardComponent,
    ProcessHeaderComponent,
    ReactiveFormsModule,
    MatFormField,
    FormsModule,
    InputComponent,
    MatSelect,
    MatInputModule,
    MatDialogActions,
    MatButtonToggleGroup,
    BasicButtonComponent,
    TableComponent,
    MatLabel,
    CustomButtonToggleComponent,
    NgTemplateOutlet,
    MatIcon,
    DatePipe,
    UpperCasePipe
  ],
  templateUrl: './enter-product-information.component.html'
})
export class EnterProductInformationComponent implements OnInit, AfterViewInit {
  temperatureProductCategory: string = 'Room Temperature'
  readonly parseTypeMap = new Map<string, string>();
  readonly fieldsMap = new Map<Field, { focus: boolean }>();
  readonly field = Field;
  totalConfiguredProduct = 10;  //TODO
  lastKeyTime = 0;
  keyPressThreshold = 50;
  productInformationForm: FormGroup;
  readonly fieldDisplayNames = {
    [Field.UNIT_NUMBER]: 'Unit Number',
    [Field.PRODUCT_CODE]: 'Product Code',
    [Field.EXPIRATION_DATE]: 'Expiration Date',
    [Field.ABO_RH]: 'ABO/RH',
    [Field.VISUAL_INSPECTION]: 'Visual Inspection',
    [Field.LICENSE_STATUS]: 'License Status'
  };

  readonly nextControlMap = {
    [Field.UNIT_NUMBER]: [Field.ABO_RH],
    [Field.ABO_RH]: [Field.PRODUCT_CODE],
    [Field.PRODUCT_CODE]: [Field.EXPIRATION_DATE]
  }

    visualInspectionOptions: ButtonOption[] = [
      {
          value: 'Satisfactory',
          class: 'toggle-green',
          iconName: 'hand-thumb-up',
          label: 'Satisfactory'
      },
      {
          value: 'Unsatisfactory',
          class: 'toggle-red',
          iconName: 'hand-thumb-down',
          label: 'Unsatisfactory'
      }
    ];

    licensedOptions: ButtonOption[] = [
      {value: 'Licensed', label: 'Licensed'},
      {value: 'Unlicensed', label: 'Unlicensed'}
    ];

    importedProducts = signal<ImportedProductInformationDTO[]>([]);
    //TODO remove mock data
    mockProductInformation = [
      {
        unitNumber: 'W23232323232',
        productCode: 'E232323232',
        description: 'description',
        aboRh: 'AB Pos',
        expirationDate: '09-20-2025',
        visualInspection: 'Satisfactory',
        isQuarantined: true,
        licenseStatus: 'Licensed'
      },
      {
        unitNumber: 'W23232323232',
        productCode: 'E232323232',
        description: 'description',
        aboRh: 'AB Pos',
        expirationDate: '09-20-2025',
        visualInspection: 'Unsatisfactory',
        isQuarantined: false,
        licenseStatus: 'Unlicensed'
      }
    ]

    expirationDateTemplateRef = viewChild<TemplateRef<Element>>('expirationDateTemplateRef');
    visualInspectionTemplateRef = viewChild<TemplateRef<Element>>('visualInspectionTemplateRef');
    quarantinedTemplateRef = viewChild<TemplateRef<Element>>('quarantinedTemplateRef');
    licenseStatusTemplateRef = viewChild<TemplateRef<Element>>('licenseStatusTemplateRef');
    inputUnitNumber = viewChild<ElementRef>('inputProductCode');
    inputAboRh = viewChild<ElementRef>('inputAboRh');
    inputProductCode = viewChild<ElementRef>('inputProductCode');
    inputExpirationDate = viewChild<ElementRef>('inputExpirationDate');

    readonly nextControlFocusMap = {
      [Field.UNIT_NUMBER]: () => this.inputAboRh()['inputFocus'] = true,
      [Field.ABO_RH]: () => this.inputProductCode()['inputFocus'] = true,
      [Field.PRODUCT_CODE]: () => this.inputExpirationDate()['inputFocus'] = true
    }

    table = viewChild<TableComponent>('importedProductInformationTable');
    importedProductsTableConfigComputed = computed<TableConfiguration>(() => ({
      title: 'Added Products',
      pageSize: 20,
      showPagination: false,
      columns: [
          {
            id: 'unitNumber',
            header: 'Unit Number',
            sort: false,
          },
          {
            id: 'productCode',
            header: 'Product Code',
            sort: false,
          },
          {
            id: 'description',
            header: 'Description',
            sort: false,
          },
          {
            id: 'aboRh',
            header: 'ABO/RH',
            sort: false,
          },
          {
            id: 'expirationDate',
            header: 'Expiration Date',
            sort: false,
            columnTempRef: this.expirationDateTemplateRef(),
          },
          {
            id: 'licenseStatus',
            header: 'License Status',
            sort: false,
            columnTempRef: this.licenseStatusTemplateRef(),
          },
          {
            id: 'visualInspection',
            header: 'Visual Inspection',
            sort: false,
            columnTempRef: this.visualInspectionTemplateRef(),
          },
          {
            id: 'isQuarantined',
            header: 'Quarantined',
            sort: false,
            columnTempRef: this.quarantinedTemplateRef(),
          }
        ],
  }));

  constructor(
    readonly fb: FormBuilder,
    private toastr: ToastrService,
    public header: ProcessHeaderService,
    private service: ReceivingService,
    private router: Router,
    private activatedRoute: ActivatedRoute
) {}


  ngAfterViewInit(): void {
    this.inputUnitNumber().nativeElement?.focus();
  }

  ngOnInit(): void {
    this.initializeForm();
    this.initMaps();
    this.importedProducts.set(this.mockProductInformation)
  }

  private initMaps() {
    this.parseTypeMap.set('unitNumber', 'BARCODE_UNIT_NUMBER');
    this.parseTypeMap.set('productCode', 'BARCODE_PRODUCT_CODE');
    this.parseTypeMap.set('expirationDate', 'BARCODE_EXPIRATION_DATE');
    this.parseTypeMap.set('aboRh', 'BARCODE_BLOOD_GROUP');

    this.fieldsMap.set(Field.UNIT_NUMBER, { focus: true });
    this.fieldsMap.set(Field.ABO_RH, { focus: false });
    this.fieldsMap.set(Field.PRODUCT_CODE, { focus: false });
    this.fieldsMap.set(Field.EXPIRATION_DATE, { focus: false });
}

  private initializeForm(): void {
    this.productInformationForm = this.fb.group({
      unitNumber: ['', [Validators.required, scannedValidatorStartWithEqual]],
      aboRh: [{ value: '', disabled: true }, [Validators.required, scannedValidatorStartWithEqual]],
      productCode: [{ value: '', disabled: true }, [Validators.required, scannedValidatorStartWithEqual]],
      expirationDate: [{ value: '', disabled: true }, [Validators.required, scannedValidatorStartWithAnd]],
      licenseStatus: ['', [Validators.required]],
      visualInspection: ['', [Validators.required]]
    });
  }

  getVisualInspectionClass(visualInspection: string){
    return visualInspectionCssMap[visualInspection.toUpperCase()];
  }

  getLicenseStatusClass(licenseStatus: string){
    return licenseStatusCssMap[licenseStatus.toUpperCase()];
  }

  getQuarantinedClass(isQuarantined: boolean){
    return quarantinedCssMap[isQuarantined.toString().toUpperCase()];
  }

  getQuarantinedValue(isQuarantined: boolean){
    return quarantinedValueMap[isQuarantined.toString().toUpperCase()];
  }

  getTemperatureProductCategoryClass(temperatureProductCategory: string){
    return temperatureProductCategoryCssMap[snakeCase(temperatureProductCategory).toUpperCase()];
  }


onTabEnter(control: Field){
    const value = this.productInformationForm.get(control).value;
    if (!value || !/^[=&]/.test(value)) {
        if (control === Field.UNIT_NUMBER) {
            this.productInformationForm.get(control).reset();
            this.fieldsMap.get(control).focus = true;
        } else if (control === Field.ABO_RH) {
          this.productInformationForm.get(control).reset();
          this.fieldsMap.get(control).focus = true;
        } else if (control === Field.PRODUCT_CODE) {
            this.productInformationForm.get(control).reset();
            this.fieldsMap.get(control).focus = true;
        } else if (control === Field.EXPIRATION_DATE) {
          this.productInformationForm.get(control).reset();
          this.fieldsMap.get(control).focus = true;
      }
        else {
            this.productInformationForm.get(control).setErrors({ invalidFormat: true });
        }
        this.fieldsMap.get(control).focus = true;
        return;
    }

    const controlValue = this.productInformationForm.get(control).value;
    const matchValue = control ? controlValue: null;
    if(matchValue && matchValue.length !== 0){
      const validations = this.createValidationObservables(control, matchValue);
      validations.subscribe((result) => {
        let hasError = false;
        const { isValid, controlKey, finalValue, notification } = result;
            if (!isValid) {
                consumeUseCaseNotifications(this.toastr, notification);
                this.productInformationForm.get(controlKey).reset();
                hasError = true;
            } else {
              this.productInformationForm.get(controlKey).setValue(finalValue);
                this.productInformationForm.get(controlKey).disable();
                if(this.nextControlMap[controlKey] !== undefined){
                  this.productInformationForm.get(this.nextControlMap[controlKey]).enable();
                  if(this.nextControlFocusMap[controlKey] !== undefined){
                    this.nextControlFocusMap[controlKey]();
                  }
                }
            }
      });
    }
  }

  private createValidationObservables(
    controlKey: Field, value: string 
  ) {
    return this.service
      .validateScannedField(this.validateRequest(controlKey, value))
      .pipe(
          map((response) => ({
              isValid: response.data.validateBarcode.data.valid,
              controlKey,
              finalValue: response.data.validateBarcode.data.resultDescription,
              notification: response.data.validateBarcode.notifications
          }))
      );
  }

  validateRequest(
    controlKey: Field, value: string 
  ): ValidateBarcodeRequestDTO{
    return {
      temperatureCategory: "FROZEN",
      barcodeValue: value,
      barcodePattern: this.parseTypeMap.get(controlKey)
    }
  }

  resetForm(){
    return this.productInformationForm.reset();
  }
}
