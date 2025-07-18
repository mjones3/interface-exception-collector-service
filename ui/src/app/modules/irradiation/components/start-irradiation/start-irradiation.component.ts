import {
    AfterViewInit,
    Component,
    effect,
    inject,
    OnInit,
    TemplateRef,
    ViewChild,
    ViewEncapsulation
} from '@angular/core';
import { EMPTY } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import {ActionButtonComponent} from "../../../../shared/components/buttons/action-button.component";
import {FuseCardComponent} from "../../../../../@fuse";
import {MatDivider} from "@angular/material/divider";
import {UnitNumberCardComponent} from "../../../../shared/components/unit-number-card/unit-number-card.component";
import {
    ProcessHeaderComponent,
    ProcessHeaderService,
    ScanUnitNumberCheckDigitComponent
} from "@shared";
import {InputComponent} from "../../../../shared/components/input/input.component";
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {ActivatedRoute, Router} from "@angular/router";
import {ToastrService} from "ngx-toastr";
import {
    IrradiationProductDTO, IrradiationResolveData,
    MessageType, StartIrradiationSubmitBatchRequestDTO, ValidateUnitEvent, ValidationDataDTO
} from "../../models/model";
import {ProductIconsService} from "../../../../shared/services/product-icon.service";
import {FuseConfirmationService} from "../../../../../@fuse/services/confirmation";
import {IrradiationService} from "../../services/irradiation.service";
import {NgStyle} from "@angular/common";
import {MatDialog} from "@angular/material/dialog";
import {IrradiationSelectProductModal} from "./select-product-modal/select-product-modal.component";
import {Cookie} from "../../../../shared/types/cookie.enum";
import {CookieService} from "ngx-cookie-service";
import {DiscardService} from "../../../../shared/services/discard.service";

const AVAILABLE = 'AVAILABLE';
const QUARANTINED = 'QUARANTINED';
const UNSUITABLE = 'UNSUITABLE';
const DISCARDED = 'DISCARDED';
const EXPIRED = 'EXPIRED';
const IRRADIATION_SUPPLY_TYPE = 'IRRADIATION_INDICATOR';

@Component({
  selector: 'biopro-start-irradiation',
  standalone: true,
    imports: [
        ActionButtonComponent,
        FuseCardComponent,
        MatDivider,
        UnitNumberCardComponent,
        ScanUnitNumberCheckDigitComponent,
        InputComponent,
        NgStyle,
        ProcessHeaderComponent,
        ReactiveFormsModule
    ],
  templateUrl: './start-irradiation.component.html',
  styleUrl: './start-irradiation.component.scss',
  encapsulation: ViewEncapsulation.None,
})
export class StartIrradiationComponent implements OnInit, AfterViewInit {

    private readonly _productIconService = inject(ProductIconsService);
    showCheckDigit = false;
    numOfMaxUnits = 0;
    selectedProducts: IrradiationProductDTO[] = [];
    products: IrradiationProductDTO[] = [];
    initialProductsState: IrradiationProductDTO[] = [];
    allProducts: IrradiationProductDTO[] = [];
    currentDateTime: string;
    startTime: string
    private isDialogOpen = false;
    @ViewChild('buttons')
    buttons: TemplateRef<Element>;

    @ViewChild('unitnumber')
    unitNumberComponent: ScanUnitNumberCheckDigitComponent;

    @ViewChild('irradiationIdInput')
    irradiationInput: InputComponent;

    @ViewChild('lotNumberInput')
    lotNumberInput: InputComponent;

    form: FormGroup;
    currentLocation: string;

    constructor(
        private readonly router: Router,
        private readonly formBuilder: FormBuilder,
        private readonly processHeaderService: ProcessHeaderService,
        private readonly irradiationService: IrradiationService,
        private readonly confirmationService: FuseConfirmationService,
        private readonly toaster: ToastrService,
        private readonly activatedRoute: ActivatedRoute,
        private readonly matDialog: MatDialog,
        private cookieService: CookieService,
        private discardService: DiscardService,
    ) {
        effect(() => {
            this.processHeaderService.setActions(this.buttons);
        });

        this.form = this.formBuilder.group({
            irradiatorId: [null, [Validators.required]],
            lotNumber: [null, [Validators.required]]
        });
    }
    ngOnInit() {
        this.showCheckDigit = (this.activatedRoute.parent?.snapshot.data?.initialData as IrradiationResolveData)
            .showCheckDigit;
        this.currentLocation = this.cookieService.get(Cookie.XFacility);
        if (!this.currentLocation) {
            this.showMessage(MessageType.ERROR, 'Location is not set. Please check your facility settings.');
            return;
        }
    }

    ngAfterViewInit(): void {
        setTimeout(() => {
            this.unitNumberComponent.form.disable();
            this.focusOnIrradiationInput();
        });
    }

    /**
     * Sets focus on the irradiator ID input field
     */
    focusOnIrradiationInput(): void {
        if (this.irradiationInput) {
            this.irradiationInput.focus();
        }
    }

    /**
     * Sets focus on the lot number input field
     */
    focusOnLotNumberInput(): void {
        if (this.lotNumberInput) {
            this.lotNumberInput.focus();
        }
    }

    get irradiation() {
        return this.form.get('irradiatorId');
    }

    get lotNumber() {
        return this.form.get('lotNumber');
    }

    openCancelConfirmationDialog(): void {
        const dialogRef = this.confirmationService.open({
            title: 'Confirmation',
            message:
                'Products added will be removed from the list without finishing the Irradiation process. Are you sure you want to continue?',
            dismissible: false,
            icon: {
                name: 'heroicons_outline:question-mark-circle',
                show: true,
                color: 'primary',
            },
            actions: {
                confirm: {
                    show: true,
                },
                cancel: {
                    show: true,
                },
            },
        });

        dialogRef.afterClosed().subscribe((result) => {
            if (result) {
                this.cancel();
            }
        });
    }

    private cancel() {
        this.resetAllData();
    }

    private resetAllData() {
        this.products = [];
        this.initialProductsState = [];
        this.selectedProducts = [];
        this.allProducts = [];
        this.irradiation.reset();
        this.irradiation.enable()
        this.lotNumber.reset();
        this.unitNumberComponent.controlUnitNumber.reset();
        this.isDialogOpen = false;
        setTimeout(() => this.focusOnIrradiationInput(), 1);
    }

    isSubmitEnabled(): boolean {
        return this.form.valid && this.numberOfUnits > 0;
    }

    get disableCancelButton() {
        //return !this.deviceId;
        return false;
    }

    submit() {
        if (this.isSubmitEnabled()) {
            const batchItems = this.products.map(product => ({
                unitNumber: product.unitNumber,
                productCode: product.productCode,
                lotNumber: this.lotNumber.value
            }));

            const request: StartIrradiationSubmitBatchRequestDTO = {
                deviceId: this.irradiation.value,
                startTime: this.startTime,
                batchItems: batchItems
            };

            this.irradiationService.startIrradiationSubmitBatch(request).subscribe({
                next: (result) => {
                    this.showMessage(MessageType.SUCCESS, (result.data as any).submitBatch.message);
                    this.resetAllData();
                    this.currentDateTime = '';
                },
                error: (error) => {
                    this.showMessage(MessageType.ERROR, error.message || 'Failed to submit irradiation batch');
                }
            });
        }
    }

    validateUnit(event: ValidateUnitEvent) {
        const { unitNumber, checkDigit, scanner } = event;
        if (!unitNumber) return;

        // Update current date and time
        const now = new Date();
        this.currentDateTime = now.toLocaleDateString('en-US', {
            month: '2-digit',
            day: '2-digit',
            year: 'numeric'
        }) + ' ' + now.toLocaleTimeString('en-US', {
            hour: '2-digit',
            minute: '2-digit',
            hour12: false
        });
        this.startTime = now.toISOString().slice(0, 19);
        if (this.showCheckDigit && !scanner) {
            this.irradiationService.validateCheckDigit(unitNumber, checkDigit).pipe(
                switchMap(result => {
                    const isValid = result.data?.checkDigit?.isValid ?? false;
                    if (!isValid) {
                        if (checkDigit) {
                            this.showMessage(MessageType.ERROR, 'Invalid check digit');
                        }
                        this.unitNumberComponent.setValidatorsForCheckDigit(false);
                        this.unitNumberComponent.focusOnCheckDigit();
                        return EMPTY;
                    }
                    return this.irradiationService.validateUnitNumber(unitNumber, this.currentLocation);
                })
            ).subscribe({
                next: this.handleValidationResult.bind(this, unitNumber),
                error: (error) => {
                    this.isDialogOpen = false;
                    this.showMessage(MessageType.ERROR, error.message);
                }
            });
        } else {
            this.irradiationService.validateUnitNumber(unitNumber, this.currentLocation).subscribe({
                next: this.handleValidationResult.bind(this, unitNumber),
                error: (error) => {
                    this.isDialogOpen = false;
                    this.showMessage(MessageType.ERROR, error.message);
                }
            });
        }
    }

    private handleValidationResult(unitNumber: string, result: any) {
        const inventories = result.data.validateUnit;
        if (inventories) {
            const irradiationProducts: IrradiationProductDTO[] = inventories.map(inventory => ({
                unitNumber: unitNumber,
                expired: inventory.expired,
                productCode: inventory.productCode,
                productDescription: inventory.productDescription,
                status: inventory.status,
                productFamily: inventory.productFamily,
                icon: this.findIconsByProductFamily(inventory.productFamily),
                order: inventory.order || 1,
                statuses: [
                    {
                        value: this.getFinalStatus(inventory),
                        classes: this.statusToColorClass(inventory),
                    },
                ],
                location: this.currentLocation,
                comments: '',
                statusReason: inventory.statusReason,
                unsuitableReason: inventory.unsuitableReason,
                alreadyIrradiated: inventory.alreadyIrradiated,
                notConfigurableForIrradiation: inventory.notConfigurableForIrradiation,
                quarantines: inventory.quarantines
            }));

            if (!this.isDialogOpen) {
                this.isDialogOpen = true;
                const defaults = {
                    height: 'auto',
                    data: {
                        options: irradiationProducts,
                        optionsLabel: 'productDescription',
                        dialogTitle: 'Select a product'
                    }
                };
                this.matDialog.open(IrradiationSelectProductModal, {
                    ...defaults
                }).afterClosed()
                    .subscribe((selectedOption) => {
                        this.isDialogOpen = false;
                        if (selectedOption) {
                            const isValid = (this.validateProduct(selectedOption) && this.isNotAnExistingIrradiatedProduct(selectedOption));
                            if (isValid) {
                                this.populateIrradiationBatch(selectedOption);
                            }
                        }
                    });
            }
        }
    }

    private validateProduct(selectedOption: IrradiationProductDTO): boolean {
        switch (selectedOption.status) {
            case DISCARDED:
                this.showMessage(MessageType.ERROR, 'This product has already been discarded for ' + selectedOption.statusReason + ' in the system. Place in biohazard container.')
                return false;
            case AVAILABLE:
                if(selectedOption.unsuitableReason) {
                    this.discardProduct(selectedOption, selectedOption.unsuitableReason)
                    return false;
                }
                if(selectedOption.expired) {
                    this.discardProduct(selectedOption, EXPIRED)
                    return false;
                }
                if(selectedOption.quarantines) {
                    return this.handleQuarantine(selectedOption);
                }
                return true;
            default:
                return true;
        }
    }

    private isNotAnExistingIrradiatedProduct (selectedOption: IrradiationProductDTO): boolean {
       if (selectedOption.alreadyIrradiated) {
           this.showMessage(MessageType.ERROR, 'This product has already been irradiated')
           return false;
       }
        if (selectedOption.notConfigurableForIrradiation) {
            this.showMessage(MessageType.ERROR, 'Product not configured for Irradiation')
            return false;
        }
        return true;
    }

    private handleQuarantine(product: IrradiationProductDTO) {
        if (product.quarantines?.some(q => q.stopsManufacturing)) {
            this.showMessage(MessageType.ERROR, 'This product has been quarantined and cannot be irradiated');
            return false;
        }
        product.status = 'Quarantined';
        return true;
    }

    private discardProduct(product: IrradiationProductDTO, reason: string) {
        const discardRequestDTO = {
            unitNumber: product.unitNumber,
            productCode: product.productCode,
            productShortDescription: product.productDescription,
            productFamily: product.productFamily,
            locationCode: product.location,
            reasonDescriptionKey: reason,
            employeeId: '4c973896-5761-41fc-8217-07c5d13a004b',
            triggeredBy: 'IRRADIATION',
            comments: ''
        };

        return this.discardService.discardProduct(discardRequestDTO).subscribe({
            next: () => {
                this.openConfirmationDialog(product);
            },
            error: (error) => {
                this.toaster.error('Unable to reach discard service.');
                throw error;
            }
        });
    }

    openConfirmationDialog(selectedProduct: IrradiationProductDTO): void {
        const dialogRef = this.confirmationService.open({
            title:
                selectedProduct.status || 'Acknowledge message',
            message: 'This product has been discarded for ' + selectedProduct.statusReason + '. Place in biohazard container',
            dismissible: false,
            icon: {
                name: 'heroicons_outline:question-mark-circle',
                show: true,
                color: 'primary'
            },
            actions: {
                confirm: {
                    label: 'Confirm',
                    show: true
                },
                cancel: {
                    label: 'Cancel',
                    show: false
                }
            }
        });

        dialogRef.afterClosed().subscribe((result) => {
            if (result) {
                this.redirect();
            }
        });
    }

    private populateIrradiationBatch(irradiationProductDTO: IrradiationProductDTO) {
        const irradiationProducts: IrradiationProductDTO[] = [irradiationProductDTO];
        const notAddedProducts = irradiationProducts.filter((p) =>
            this.notInProductList(p)
        );

        if (notAddedProducts.length === 0) {
            this.showMessage(
                MessageType.WARNING,
                'Product has already been added to the list'
            );
            return;
        }

        irradiationProducts.forEach((product) => {
            this.addProductToList(product);
        });
    }

    private findIconsByProductFamily(productFamily: string) {
        return this._productIconService.getIconByProductFamily(productFamily);
    }

    private getFinalStatus(inventory: IrradiationProductDTO) {
        if (inventory.expired) {
            return EXPIRED;
        }
        if (inventory.unsuitableReason) {
            return UNSUITABLE;
        }
        if (inventory.quarantines && inventory.quarantines.length !==0) {
            return QUARANTINED;
        }
        if (inventory.status === DISCARDED) {
            return DISCARDED;
        }
        return AVAILABLE;
    }

    private statusToColorClass(inventory: IrradiationProductDTO) {
        if (inventory.expired || inventory.unsuitableReason || inventory.status === DISCARDED) {
            return 'bg-red-500 text-white';
        }
        if (inventory.quarantines && inventory.quarantines.length !==0) {
            return 'bg-orange-500 text-white';
        }
        return 'bg-green-500 text-white';

    }

    private addProductToList(newProduct: IrradiationProductDTO) {
        this.initialProductsState.push({ ...newProduct });
        this.products.push(newProduct);
        this.products = this.products.sort((productA, productB) => {
            return productB.order - productA.order;
        });

        this.unitNumberComponent.reset();
        this.unitNumberComponent.focusOnUnitNumber();

        this.allProducts.push({ ...newProduct });
    }

    private notInProductList(product: ValidationDataDTO) {
        return !this.products.find(
            (p) =>
                p.productCode === product.productCode &&
                p.unitNumber === product.unitNumber
        );
    }

    get numberOfUnits() {
        return this.allProducts
            .filter(p => !p.disabled)
            .length;
    }

    get selectAllTextRule() {
        return (
            this.numberOfSelectedUnits < this.numberOfUnits ||
            this.numberOfUnits === 0
        );
    }

    get numberOfSelectedUnits() {
        return this.selectedProducts.length;
    }

    selectAllUnits() {
        if (this.selectedProducts.length === this.products.length) {
            this.selectedProducts = [];
        } else {
            this.selectedProducts = [].concat(this.products.filter(p => !p.disabled));
        }
    }

    openRemoveConfirmationDialog(): void {
        const dialogRef = this.confirmationService.open({
            title: 'Confirmation',
            message:
                'All changes will be removed without finishing the irradiation process. Are you sure you want to continue?',
            dismissible: false,
            icon: {
                name: 'heroicons_outline:question-mark-circle',
                show: true,
                color: 'primary',
            },
            actions: {
                confirm: {
                    label: 'Confirm',
                    show: true,
                },
                cancel: {
                    label: 'Cancel',
                    show: true,
                },
            },
        });

        dialogRef.afterClosed().subscribe((result) => {
            if (result) {
                this.removeSelected();
                this.selectedProducts = [];
            }
        });
    }

    private removeSelected() {
        while (this.selectedProducts.length > 0) {
            const index = this.products.indexOf(this.selectedProducts[0]);
            this.products.splice(index, 1);
            this.selectedProducts.splice(0, 1);
        }

        this.allProducts = [];
        this.allProducts = [...this.products];

        this.selectedProducts = [];
    }

    toggleProduct(product: IrradiationProductDTO) {
        if (this.selectedProducts.includes(product)) {
            const index = this.selectedProducts.findIndex(
                (filterProduct) =>
                    filterProduct.unitNumber === product.unitNumber &&
                    filterProduct.productCode === product.productCode
            );
            this.selectedProducts.splice(index, 1);
        } else {
            this.selectedProducts.push(product);
        }
    }

    loadIrradiationId(deviceId: string) {
       if (deviceId) {
           this.irradiationService.loadDeviceById(deviceId,this.currentLocation).subscribe({
               next: (result) => {
                   const validDevice = result.data.validateDevice;
                   if (validDevice) {
                       this.irradiation.disable();
                       this.lotNumber.enable();
                       setTimeout(() => this.focusOnLotNumberInput(), 0);
                   }
               },
               error: (error) => {
                   this.showMessage(MessageType.ERROR, error.message)
               }
           })
       }
    }

    redirect() {
        this.router.navigateByUrl('irradiation/start-irradiation');
    }

    private showMessage(messageType: MessageType, message: string) {
        if (messageType === MessageType.ERROR) {
            this.toaster.error(message);
        } else if (messageType === MessageType.WARNING) {
            this.toaster.warning(message);
        } else {
            this.toaster.success(message);
        }
    }

    validateLotNumber(lotNumber: string) {
        if (lotNumber) {
            this.irradiationService.validateLotNumber(lotNumber, IRRADIATION_SUPPLY_TYPE).subscribe({
                next: (result) => {
                    const isValid = result.data.validateLotNumber;
                    if (isValid) {
                        this.unitNumberComponent.controlUnitNumber.enable();
                        setTimeout(() => this.unitNumberComponent.focusOnUnitNumber(), 0);
                    } else {
                        this.showMessage(MessageType.ERROR, 'Invalid lot number');
                        this.lotNumber.setErrors({ invalid: true });
                    }
                },
                error: (error) => {
                    this.showMessage(MessageType.ERROR, error.message || 'Failed to validate lot number');
                    this.lotNumber.setErrors({ invalid: true });
                }
            });
        }
    }
}
