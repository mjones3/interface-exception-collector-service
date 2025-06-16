import { AsyncPipe, DatePipe, NgTemplateOutlet, UpperCasePipe } from '@angular/common';
import { AfterViewInit, Component, computed, OnInit, signal, TemplateRef, viewChild, ElementRef } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonToggleGroup } from '@angular/material/button-toggle';
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
import { AddImportItemRequestDTO, CreateImportResponsetDTO, ImportedItemResponseDTO } from '../../models/product-information.dto';
import { MatIcon } from '@angular/material/icon';
import { licenseStatusCssMap, quarantinedCssMap, quarantinedValueMap, temperatureProductCategoryCssMap, TemperatureProductCategoryValueMap, visualInspectionCssMap } from '../../graphql/query-definitions/imports-enter-shipping-information.graphql';
import { snakeCase } from 'lodash';
import { FuseCardComponent } from '@fuse/components/card/public-api';
import { scannedValidatorStartWithAnd, scannedValidatorStartWithEqual } from 'app/shared/forms/biopro-validators';
import { catchError, map, Observable, switchMap, take, tap } from 'rxjs';
import { ReceivingService } from '../../service/receiving.service';
import { ValidateBarcodeRequestDTO } from '../../graphql/query-definitions/validate-bar-code.graphql';
import { consumeUseCaseNotifications } from 'app/shared/utils/notification.handling';
import { Store } from '@ngrx/store';
import { getAuthState } from 'app/core/state/auth/auth.selectors';
import { ApolloError } from '@apollo/client';
import handleApolloError from 'app/shared/utils/apollo-error-handling';
import { UseCaseNotificationDTO } from '../../../../shared/models/use-case-response.dto';

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
  readonly parseTypeMap = new Map<string, string>();
  readonly fieldsMap = new Map<Field, { focus: boolean }>();
  protected readonly TemperatureProductCategoryValueMap = TemperatureProductCategoryValueMap;
  readonly field = Field;
  productInformationForm: FormGroup;
  addImportItemRequest = signal<AddImportItemRequestDTO>({} as AddImportItemRequestDTO);
  importData = signal<CreateImportResponsetDTO>({} as CreateImportResponsetDTO)
  employeeId: string;
  routeIdComputed = computed(() => Number(this.route?.snapshot?.params?.id));

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
          value: 'SATISFACTORY',
          class: 'toggle-green',
          iconName: 'hand-thumb-up',
          label: 'Satisfactory'
      },
      {
          value: 'UNSATISFACTORY',
          class: 'toggle-red',
          iconName: 'hand-thumb-down',
          label: 'Unsatisfactory'
      }
    ];

    licenseOptions: ButtonOption[] = [
      {value: 'LICENSED', label: 'Licensed'},
      {value: 'UNLICENSED', label: 'Unlicensed'}
    ];

    expirationDateTemplateRef = viewChild<TemplateRef<Element>>('expirationDateTemplateRef');
    visualInspectionTemplateRef = viewChild<TemplateRef<Element>>('visualInspectionTemplateRef');
    quarantinedTemplateRef = viewChild<TemplateRef<Element>>('quarantinedTemplateRef');
    licenseStatusTemplateRef = viewChild<TemplateRef<Element>>('licenseStatusTemplateRef');
    inputUnitNumber = viewChild<ElementRef>('inputUnitNumber');
    inputAboRh = viewChild<ElementRef>('inputAboRh');
    inputProductCode = viewChild<ElementRef>('inputProductCode');
    inputExpirationDate = viewChild<ElementRef>('inputExpirationDate');

    readonly nextControlFocusMap = {
      [Field.UNIT_NUMBER]: () => this.inputAboRh()['inputFocus'] = true,
      [Field.ABO_RH]: () => this.inputProductCode()['inputFocus'] = true,
      [Field.PRODUCT_CODE]: () => this.inputExpirationDate()['inputFocus'] = true
    }

    table = viewChild<TableComponent>('importedProductInformationTable');
    importItemsTableConfigComputed = computed<TableConfiguration>(() => ({
      title: 'Added Products',
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
            id: 'productDescription',
            header: 'Description',
            sort: false,
          },
          {
            id: 'aboRh',
            header: 'ABO/Rh',
            sort: false,
          },
          {
            id: 'expirationDate',
            header: 'Expiration Date',
            sort: false,
            columnTempRef: this.expirationDateTemplateRef()
          },
          {
            id: 'licenseStatus',
            header: 'License Status',
            sort: false,
            columnTempRef: this.licenseStatusTemplateRef()
          },
          {
            id: 'visualInspection',
            header: 'Visual Inspection',
            sort: false,
            columnTempRef: this.visualInspectionTemplateRef()
          },
          {
            id: 'isQuarantined',
            header: 'Quarantined',
            sort: false,
            columnTempRef: this.quarantinedTemplateRef()
          }
        ],
  }));

  constructor(
    readonly fb: FormBuilder,
    private toastr: ToastrService,
    public header: ProcessHeaderService,
    private service: ReceivingService,
    protected route: ActivatedRoute,
    private store: Store,
  ) {
    this.setEmployeeId();
  }

  private setEmployeeId() {
    this.store
        .select(getAuthState)
        .pipe(take(1))
        .subscribe((auth) => {
            this.employeeId = auth['id'];
        });
  }

  ngAfterViewInit(): void {
    this.inputUnitNumber().nativeElement?.focus();
  }

  ngOnInit(): void {
    this.initializeForm();
    this.initMaps();
    this.loadImportDetails(this.routeIdComputed())
    .subscribe();
  }

  loadImportDetails(
      id: number
  ): Observable<CreateImportResponsetDTO> {
      return this.service.getImportById(id)
      .pipe(
          catchError((error: ApolloError) => {
              handleApolloError(this.toastr, error);
          }),
          tap(data => consumeUseCaseNotifications(this.toastr, data.data?.findImportById.notifications)),
          map((response) => {
              const { data } = response.data.findImportById;
              this.importData.set(data)
              return data;
          })
      );
  }

  private initMaps() {
    this.parseTypeMap.set('unitNumber', 'BARCODE_UNIT_NUMBER');
    this.parseTypeMap.set('productCode', 'BARCODE_PRODUCT_CODE');
    this.parseTypeMap.set('expirationDate', 'BARCODE_EXPIRATION_DATE');
    this.parseTypeMap.set('aboRh', 'BARCODE_BLOOD_GROUP');

    this.fieldsMap.set(Field.UNIT_NUMBER, { focus: true,});
    this.fieldsMap.set(Field.ABO_RH, { focus: false });
    this.fieldsMap.set(Field.PRODUCT_CODE, { focus: false });
    this.fieldsMap.set(Field.EXPIRATION_DATE, { focus: false });
  }

  private initializeForm(): void {
    this.productInformationForm = this.fb.group({
      unitNumber: ['', [Validators.required, scannedValidatorStartWithEqual]],
      aboRh: ['', [Validators.required, scannedValidatorStartWithEqual]],
      productCode: ['', [Validators.required, scannedValidatorStartWithEqual]],
      expirationDate: ['', [Validators.required, scannedValidatorStartWithAnd]],
      licenseStatus: ['', [Validators.required]],
      visualInspection: ['', [Validators.required]]
    });
    this.disableField();
  }

  disableField(){
    this.productInformationForm.get(Field.ABO_RH).disable();
    this.productInformationForm.get(Field.PRODUCT_CODE).disable();
    this.productInformationForm.get(Field.EXPIRATION_DATE).disable();
  }

  getVisualInspectionClass(visualInspection: string){
    return visualInspectionCssMap[visualInspection];
  }

  getLicenseStatusClass(licenseStatus: string){
    return licenseStatusCssMap[licenseStatus];
  }

  getQuarantinedClass(isQuarantined: boolean){
    return quarantinedCssMap[isQuarantined?.toString().toUpperCase()];
  }

  getQuarantinedValue(isQuarantined: boolean){
    return quarantinedValueMap[isQuarantined?.toString().toUpperCase()];
  }

  getTemperatureProductCategoryClass(temperatureProductCategory: string){
    return temperatureProductCategoryCssMap[snakeCase(temperatureProductCategory).toUpperCase()];
  }

  onTabEnter(control: Field){
    const value = this.productInformationForm.get(control).value;
    if (!value || !/^[=&]/.test(value)) {
        if (control === Field.UNIT_NUMBER) {
          this.fieldsMap.get(control).focus = true;
        } else if (control === Field.ABO_RH) {
          this.fieldsMap.get(control).focus = true;
        } else if (control === Field.PRODUCT_CODE) {
            this.fieldsMap.get(control).focus = true;
        } else if (control === Field.EXPIRATION_DATE) {
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
        const { isValid, controlKey, finalValue, requestValue } = result;
            if (!isValid) {
                this.productInformationForm.get(controlKey).reset();
            } else {
                this.productInformationForm.get(controlKey).setValue(finalValue);
                this.addImportItemRequest()[controlKey] = requestValue;
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

  private createValidationObservables(controlKey: Field, value: string) {
    return this.service
      .validateScannedField(this.validateRequest(controlKey, value))
      .pipe(
        catchError((error: ApolloError) => {handleApolloError(this.toastr, error)}),
        tap(data => consumeUseCaseNotifications(this.toastr, data.data?.validateBarcode.notifications)),
        map(({ data }) => {
          const { valid, result, resultDescription } = data.validateBarcode.data;
          return {
            isValid: valid,
            controlKey,
            finalValue: resultDescription !== null ? resultDescription : result,
            requestValue: result,
          };
        })
      );
  }

  validateRequest(
    controlKey: Field, value: string
  ): ValidateBarcodeRequestDTO{
    return {
      temperatureCategory: this.importData().temperatureCategory,
      barcodeValue: value,
      barcodePattern: this.parseTypeMap.get(controlKey)
    }
  }

  resetForm(){
    this.productInformationForm.reset();
    this.productInformationForm.get(Field.UNIT_NUMBER).enable();
    this.disableField();
    this.inputUnitNumber()['inputFocus'] = true;
  }

  hasValues(): boolean {
    const controls = this.productInformationForm.controls;
    for (const controlName in controls) {
      if (controls.hasOwnProperty(controlName)) {
        if (controls[controlName]?.value?.length > 0) {
          return true;
        }
      }
    }
    return false;
  }

  isFormValid(): boolean {
    const fieldValidatedWithValues = [
      Field.UNIT_NUMBER,
      Field.ABO_RH,
      Field.PRODUCT_CODE,
      Field.EXPIRATION_DATE
    ];

    const fieldsValid = fieldValidatedWithValues.every(field => {
      const control = this.productInformationForm.get(field);
      return control.disabled && control.value && !control.errors;
    });

    const otherFields = [
      Field.LICENSE_STATUS,
      Field.VISUAL_INSPECTION
    ];

    const otherFieldsValid = otherFields.every(field => {
      const control = this.productInformationForm.get(field);
      return control.value && !control.errors;
    });

    return fieldsValid && otherFieldsValid;
  }

  addImportItems(){
    const req = this.prepareAddProductReq();
    this.service.addImportItems(req)
    .pipe(
      catchError((error: ApolloError) => {
          if(error.message.includes("R2DBC commit")){
              const notification = [{
                  type: "WARN",
                  message: "Product already added"
              }] as UseCaseNotificationDTO[]
              consumeUseCaseNotifications(this.toastr, notification)
              throw error;
          }else{
              handleApolloError(this.toastr, error)
          }

        }
      ),
    ).subscribe((response) => {
      if(response.data?.createImportItem?.notifications[0]?.type === 'SUCCESS'){
        this.resetForm();
        this.importData.set(response.data.createImportItem.data)
      } else {
        consumeUseCaseNotifications(this.toastr, response.data?.createImportItem?.notifications)
      }
    });
  }

  prepareAddProductReq(): AddImportItemRequestDTO{
    this.addImportItemRequest().licenseStatus = this.productInformationForm.get('licenseStatus').value;
    this.addImportItemRequest().visualInspection = this.productInformationForm.get('visualInspection').value;
    this.addImportItemRequest().employeeId = this.employeeId;
    this.addImportItemRequest().importId = this.importData().id;
    return this.addImportItemRequest();
  }
}
