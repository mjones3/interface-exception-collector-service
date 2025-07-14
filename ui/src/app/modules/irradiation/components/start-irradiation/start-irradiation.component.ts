import {
    AfterViewInit,
    Component,
    effect,
    inject,
    Input,
    OnInit,
    TemplateRef,
    ViewChild,
    ViewEncapsulation
} from '@angular/core';
import {ActionButtonComponent} from "../../../../shared/components/buttons/action-button.component";
import {FuseCardComponent} from "../../../../../@fuse";
import {MatDivider} from "@angular/material/divider";
import {UnitNumberCardComponent} from "../../../../shared/components/unit-number-card/unit-number-card.component";
import {
    FacilityService,
    ProcessHeaderComponent,
    ProcessHeaderService,
    ScanUnitNumberCheckDigitComponent
} from "@shared";
import {InputComponent} from "../../../../shared/components/input/input.component";
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {ActivatedRoute, Router} from "@angular/router";
import {ToastrService} from "ngx-toastr";
import {
    IrradiationProductDTO,
    MessageType, ValidateUnitEvent, ValidationDataDTO
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
const SHIPPED = 'SHIPPED';
const EXPIRED = 'EXPIRED';

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
    isCheckDigitVisible = true;
    numOfMaxUnits = 0;
    selectedProducts: IrradiationProductDTO[] = [];
    products: IrradiationProductDTO[] = [];
    initialProductsState: IrradiationProductDTO[] = [];
    allProducts: IrradiationProductDTO[] = [];
    deviceId: string;

    @Input() showCheckDigit = true;

    @ViewChild('buttons')
    buttons: TemplateRef<Element>;

    @ViewChild('unitnumber')
    unitNumberComponent: ScanUnitNumberCheckDigitComponent;

    form: FormGroup;
    currentLocation: string;

    constructor(
        private readonly router: Router,
        private readonly formBuilder: FormBuilder,
        private readonly processHeaderService: ProcessHeaderService,
        private readonly irradiationService: IrradiationService,
        private readonly confirmationService: FuseConfirmationService,
        private readonly toaster: ToastrService,
        private readonly facilityService: FacilityService,
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
        this.isCheckDigitVisible = (
            this.activatedRoute.snapshot.data as { useCheckDigit: boolean }
        )?.useCheckDigit;
        this.currentLocation = this.cookieService.get(Cookie.XFacility);
    }

    ngAfterViewInit(): void {
        setTimeout(() => this.unitNumberComponent.form.disable());
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
        this.lotNumber.reset();
        this.unitNumberComponent.reset();
        //this.redirect();
    }

    isSubmitEnabled(): boolean {
        return this.form.valid && this.numberOfUnits > 0;
    }

    get disableCancelButton() {
        //return !this.deviceId;
        return false;
    }

    submit() {
        const unitNumbers = this.products.map((product) => product.unitNumber);
        const requestDTO = {
            unitNumbers: unitNumbers,
            location: this.facilityService.getFacilityCode(),
            deviceId: this.deviceId,
        };
    }

    validateUnit(event: ValidateUnitEvent) {
        const unitNumber = event.unitNumber;
        if (unitNumber) {
            this.irradiationService.validateUnit(unitNumber,this.currentLocation).subscribe({
                next: (result) => {
                    const inventories = result.data.products;
                    if (inventories) {
                        const irradiationProducts: IrradiationProductDTO[] = inventories.map(inventory => ({
                            unitNumber: unitNumber,
                            productCode: inventory.productCode,
                            productDescription: inventory.productDescription,
                            status: inventory.status,
                            productFamily: inventory.productFamily,
                            icon: this.findIconsByProductFamily(inventory.productFamily),
                            order: inventory.order || 1,
                            statuses: [
                                {
                                    value: inventory.status,
                                    classes: this.statusToColorClass(inventory.status),
                                },
                            ],
                            location: this.currentLocation,
                            comments: '',
                            statusReason: '',
                            unsuitableReason: ''
                        }))

                        const defaults = {
                            height: 'auto',
                            data: {
                                options: irradiationProducts,
                                optionsLabel: 'productDescription'
                            }
                        };

                        this.matDialog.open(IrradiationSelectProductModal, {
                            ...defaults
                        }).afterClosed()
                            .subscribe((selectedOption) => {
                                if (selectedOption) {
                                    this.validateProduct(selectedOption);
                                    this.populateCentrifugationBatch(selectedOption);
                                }
                            });

                    }
                },
                error: (error) => {
                    this.showMessage(MessageType.ERROR, error.message)
                }
            })
        }

    }

    private validateProduct(selectedOption: IrradiationProductDTO) {
        switch (selectedOption.status) {
            case DISCARDED:
                return this.discardProduct(selectedOption);
            case QUARANTINED:
                return this.handleQuarantine(selectedOption);
            case UNSUITABLE:
                return this.handleUnsuitableProduct(selectedOption);
            default:
                return this.toaster.error(selectedOption.statusReason);
        }
    }

    private handleQuarantine(product: IrradiationProductDTO) {
        console.log('handleQuarantine', product);
    }

    private handleUnsuitableProduct(product: IrradiationProductDTO) {
        this.discardProduct(product);
    }

    private discardProduct(product: IrradiationProductDTO) {
        const discardRequestDTO = {
            unitNumber: product.unitNumber,
            productCode: product.productCode,
            productShortDescription: product.productDescription,
            productFamily: product.productFamily,
            locationCode: product.location,
            reasonDescriptionKey: product.statusReason,
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
                selectedProduct.productDescription || 'Acknowledge message',
            message: selectedProduct.statusReason,
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
                console.log('Confirm');
                // this.router.navigateByUrl(
                //     `/labeling/temperature-category/scan-unit-number/${this.categoryId}`
                // );
            }
        });
    }

    private populateCentrifugationBatch(irradiationProductDTO: IrradiationProductDTO) {
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

    private statusToColorClass(status: string) {
        switch (status) {
            case SHIPPED:
                return 'bg-orange-500 text-white';
            case QUARANTINED:
                return 'bg-orange-500 text-white';
            case AVAILABLE:
                return 'bg-green-500 text-white';
            case UNSUITABLE:
            case DISCARDED:
            case EXPIRED:
                return 'bg-red-500 text-white';


        }
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
                   }
               },
               error: (error) => {
                   this.showMessage(MessageType.ERROR, error.message)
               }
           })
       }
    }

    redirect() {
        this.router.navigateByUrl('irradiation');
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

    validateLotNumber($event: string) {
       console.log('validateLotNumber', $event);
        this.unitNumberComponent.controlUnitNumber.enable();
    }
}
