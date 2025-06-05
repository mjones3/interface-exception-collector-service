import { AsyncPipe, DatePipe, NgTemplateOutlet, UpperCasePipe } from '@angular/common';
import { Component, computed, OnInit, signal, TemplateRef, viewChild } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonToggleGroup, MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatDialogActions } from '@angular/material/dialog';
import { MatFormField, MatLabel } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelect } from '@angular/material/select';
import { ActivatedRoute, Router } from '@angular/router';
import { ProcessHeaderComponent, ProcessHeaderService, TableConfiguration } from '@shared';
import { ActionButtonComponent } from 'app/shared/components/buttons/action-button.component';
import { BasicButtonComponent } from 'app/shared/components/buttons/basic-button.component';
import { CustomButtonToggleComponent } from 'app/shared/components/custom-button-toggle/custom-button-toggle.component';
import { InputComponent } from 'app/shared/components/input/input.component';
import { TableComponent } from 'app/shared/components/table/table.component';
import { ButtonOption } from 'app/shared/models/custom-button-toggle.model';
import { ToastrService } from 'ngx-toastr';
import { ImportedProductInformationDTO } from '../../models/product-information.dto';
import { MatIcon } from '@angular/material/icon';
import { licenseStatusCssMap, temperatureProductCategoryCssMap, visualInspectionCssMap } from '../../graphql/query-definitions/imports-enter-shipping-information.graphql';
import { snakeCase } from 'lodash';
import { FuseCardComponent } from '@fuse/components/card/public-api';


export enum Field {
  UNIT_NUMBER = 'unitNumber',
  PRODUCT_CODE = 'productCode',
  EXPIRATION_DATE = 'expirationDate',
  BLOOD_TYPE = 'bloodType',
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
    MatButtonToggleModule,
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
export class EnterProductInformationComponent implements OnInit {
  temperatureProductCategory = 'Room Temperature'
  readonly fieldsMap = new Map<Field, { focus: boolean }>();
  readonly field = Field;
  lastKeyTime = 0;
  barcode = '';
  keyPressThreshold = 50;
  productInformationForm: FormGroup;
  manualEntryAllowed: boolean;
  readonly fieldDisplayNames = {
    [Field.UNIT_NUMBER]: 'Unit Number',
    [Field.PRODUCT_CODE]: 'Product Code',
    [Field.EXPIRATION_DATE]: 'Expiration Date',
    [Field.BLOOD_TYPE]: 'Blood Type',
    [Field.VISUAL_INSPECTION]: 'Visual Inspection',
    [Field.LICENSE_STATUS]: 'License Status'
};

    visualInspectionOptions: ButtonOption[] = [
      {
          value: 'Satisfactory',
          class: 'success-green',
          iconName: 'hand-thumb-up',
          label: 'Satisfactory'
      },
      {
          value: 'Unsatisfactory',
          class: 'unsuccess-red',
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
        bloodType: 'AB Pos',
        expirationDate: '09-20-2025',
        visualInspection: 'Satisfactory',
        quarantined: true,
        licenseStatus: 'Licensed'
      },
      {
        unitNumber: 'W23232323232',
        productCode: 'E232323232',
        description: 'description',
        bloodType: 'AB Pos',
        expirationDate: '09-20-2025',
        visualInspection: 'Unsatisfactory',
        quarantined: false,
        licenseStatus: 'Unlicensed'
      }
    ]

    expirationDateTemplateRef = viewChild<TemplateRef<Element>>('expirationDateTemplateRef');
    visualInspectionTemplateRef = viewChild<TemplateRef<Element>>('visualInspectionTemplateRef');
    quarantinedTemplateRef = viewChild<TemplateRef<Element>>('quarantinedTemplateRef');
    licenseStatusTemplateRef = viewChild<TemplateRef<Element>>('licenseStatusTemplateRef');
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
            id: 'bloodType',
            header: 'Blood Type',
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
            id: 'quarantined',
            header: 'Quarantined',
            sort: false,
            columnTempRef: this.quarantinedTemplateRef(),
          }
        ],
  }));

  constructor(
    readonly fb: FormBuilder,
    private toaster: ToastrService,
    public header: ProcessHeaderService,
    private router: Router,
    private activatedRoute: ActivatedRoute
) {}

  ngOnInit(): void {
    this.initializeForm();
    this.importedProducts.set(this.mockProductInformation)
  }

  private initializeForm(): void {
    this.productInformationForm = this.fb.group({
      unitNumber: ['', [Validators.required]],
      productCode: ['', [Validators.required]],
      bloodType: ['', [Validators.required]],
      expirationDate: ['', [Validators.required]],
      licenseStatus: ['', [Validators.required]],
      visualInspection: ['', [Validators.required]]
  });
  this.productInformationForm.get('productCode').disable();
  this.productInformationForm.get('bloodType').disable();
  this.productInformationForm.get('expirationDate').disable();
  }

  getVisualInspectionClass(visualInspection: string){
    return visualInspectionCssMap[visualInspection.toUpperCase()];
  }

  getLicenseStatusClass(licenseStatus: string){
    return licenseStatusCssMap[licenseStatus.toUpperCase()];
  }

  getTemperatureProductCategoryClass(temperatureProductCategory: string){
    return temperatureProductCategoryCssMap[snakeCase(temperatureProductCategory).toUpperCase()];
  }


  onKeyDown(control: Field, event: KeyboardEvent) {
    if (!this.manualEntryAllowed) {
        if (
            event.key === '=' ||
            event.key === '&' ||
            event.key === 'Shift'
        ) {
            this.lastKeyTime = 0;
        }

        const currentTime = new Date().getTime();
        const timeDiff = currentTime - this.lastKeyTime;

        if (this.lastKeyTime !== 0) {
            if (timeDiff < this.keyPressThreshold) {
                this.barcode += event.key;
            } else {
                this.toaster.error('Manual Entry Not Allowed.');
                this.productInformationForm.get(control).reset();
            }
            if (event.key === 'Enter') {
                this.barcode = '';
            }
        }
        this.lastKeyTime = currentTime;
    }
}

onTabEnter(control){
    // TODO
    const value = this.productInformationForm.get(control).value;
    if (!value || !/^[=&]/.test(value)) {
        if (control === Field.UNIT_NUMBER) {
            this.productInformationForm.get(control).reset();
            this.fieldsMap.get(control).focus = true;
            this.toaster.error(`Unit Number is invalid.`);
        } else if (control === Field.BLOOD_TYPE) {
          this.productInformationForm.get(control).reset();
          this.fieldsMap.get(control).focus = true;
          this.toaster.error(`Blood Type is invalid.`);
        } else if (control === Field.PRODUCT_CODE) {
            this.productInformationForm.get(control).reset();
            this.fieldsMap.get(control).focus = true;
            this.toaster.error(`Product Type is invalid.`);
        } else if (control === Field.EXPIRATION_DATE) {
          this.productInformationForm.get(control).reset();
          this.fieldsMap.get(control).focus = true;
          this.toaster.error(`Expiration Date is invalid.`);
      }
        else {
            this.productInformationForm.get(control).setErrors({ invalidFormat: true });
        }
        this.fieldsMap.get(control).focus = true;
        return;
    }
    this.validateSingleField(control, value);
  }

  onPaste(control: Field) {
    if (!this.manualEntryAllowed) {
        this.toaster.error('Manual Entry Not Allowed.');
        this.productInformationForm.get(control).reset();
    }
}

private validateSingleField(control: Field, value: string) {
  //TODO
  if (control === Field.EXPIRATION_DATE) {
      const regexExpiration = /^&>\d{10}$/;
      if (!regexExpiration.test(value)) {
          this.productInformationForm.get(control).reset();
          this.fieldsMap.get(control).focus = true;
          this.toaster.error(`Expiration Date is invalid.`);
          return;
      }
  }
}
}
