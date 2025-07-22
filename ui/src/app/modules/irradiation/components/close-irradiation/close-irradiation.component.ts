import {
    AfterViewInit,
    Component,
    effect,
    inject,
    OnInit,
    TemplateRef,
    ViewChild, ViewEncapsulation
} from '@angular/core';
import {ActionButtonComponent} from "../../../../shared/components/buttons/action-button.component";
import {FuseCardComponent} from "../../../../../@fuse";
import {InputComponent} from "../../../../shared/components/input/input.component";
import {MatDivider} from "@angular/material/divider";
import {
    ProcessHeaderComponent,
    ProcessHeaderService,
    ScanUnitNumberCheckDigitComponent
} from "@shared";
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {UnitNumberCardComponent} from "../../../../shared/components/unit-number-card/unit-number-card.component";
import {ProductIconsService} from "../../../../shared/services/product-icon.service";
import {
    IrradiationProductDTO, IrradiationResolveData,
    MessageType, RecordVisualInpectionResult, ValidateUnitEvent,
    ValidationDataDTO
} from "../../models/model";
import {ActivatedRoute, Router} from "@angular/router";
import {IrradiationService} from "../../services/irradiation.service";
import {FuseConfirmationService} from "../../../../../@fuse/services/confirmation";
import {ToastrService} from "ngx-toastr";
import {MatDialog} from "@angular/material/dialog";
import {NgStyle} from "@angular/common";
import {
    RecordVisualInspectionModalComponent
} from "../record-visual-inspection-modal/record-visual-inspection-modal.component";
import {EMPTY} from "rxjs";

const AVAILABLE = 'AVAILABLE';
const QUARANTINED = 'QUARANTINED';
const PENDING_INSPECTION = 'PENDING INSPECTION';
const EXPIRED = 'EXPIRED';
const IRRADIATED = 'IRRADIATED';
const NOT_IRRADIATED = 'NOT IRRADIATED';
const UNSUITABLE = 'UNSUITABLE';
const DISCARDED = 'DISCARDED';

@Component({
    selector: 'biopro-close-irradiation',
    standalone: true,
    imports: [
        ActionButtonComponent,
        FuseCardComponent,
        InputComponent,
        MatDivider,
        ProcessHeaderComponent,
        ReactiveFormsModule,
        ScanUnitNumberCheckDigitComponent,
        UnitNumberCardComponent,
        NgStyle
    ],
    templateUrl: './close-irradiation.component.html',
    styleUrl: './close-irradiation.component.scss',
    encapsulation: ViewEncapsulation.None,
})
export class CloseIrradiationComponent implements OnInit, AfterViewInit {

    private readonly _productIconService = inject(ProductIconsService);
    numOfMaxUnits = 0;
    selectedProducts: IrradiationProductDTO[] = [];
    products: IrradiationProductDTO[] = [];
    initialProductsState: IrradiationProductDTO[] = [];
    allProducts: IrradiationProductDTO[] = [];
    currentDateTime: string;
    deviceId: boolean = false;
    startTime: string
    showCheckDigit = false

    @ViewChild('buttons')
    buttons: TemplateRef<Element>;

    @ViewChild('unitnumber')
    unitNumberComponent: ScanUnitNumberCheckDigitComponent;
    form: FormGroup;

    constructor(
        private readonly router: Router,
        private readonly formBuilder: FormBuilder,
        private readonly processHeaderService: ProcessHeaderService,
        private readonly confirmationService: FuseConfirmationService,
        private readonly toaster: ToastrService,
        private readonly activatedRoute: ActivatedRoute,
        private readonly matDialog: MatDialog,
        private readonly irradiationService: IrradiationService,
    ) {
        effect(() => {
            this.processHeaderService.setActions(this.buttons);
        });

        this.form = this.formBuilder.group({
            irradiatorId: [null, [Validators.required]],
        });
    }

    ngOnInit() {
        this.showCheckDigit = (this.activatedRoute.parent?.snapshot.data?.initialData as IrradiationResolveData)
            .showCheckDigit;
    }

    ngAfterViewInit(): void {
        setTimeout(() => this.unitNumberComponent.form.disable());
    }

    get irradiation() {
        return this.form.get('irradiatorId');
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
        this.deviceId = false;
        this.products = [];
        this.initialProductsState = [];
        this.selectedProducts = [];
        this.allProducts = [];
        this.unitNumberComponent.reset();
        this.redirect();
    }

    isSubmitEnabled(): boolean {
        if (this.form.valid && this.numberOfUnits > 0) {
            const enabledProducts = this.products.filter(p => !p.disabled);
            return enabledProducts.every(product =>
                product.statuses.some(status =>
                    status.value === IRRADIATED || status.value === NOT_IRRADIATED
                )
            );
        }
        return false
    }

    get disableCancelButton() {
        return !this.deviceId;
    }

    submit() {
        //TODO: add here the submit endpoint
        this.currentDateTime = ''
        this.showMessage(MessageType.SUCCESS, 'Batch successfully closed. Label irradiated products.');
        this.redirect();
    }


    validateUnit(event: ValidateUnitEvent) {
        const { unitNumber, checkDigit, scanner } = event;
        if (!unitNumber) return;
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
            this.irradiationService.validateCheckDigit(unitNumber, checkDigit).subscribe({
                next: (result) => {
                    const isValid = result.data?.checkDigit?.isValid ?? false;
                    if (!isValid) {
                        if (checkDigit) {
                            this.showMessage(MessageType.ERROR, 'Invalid check digit');
                        }
                        this.unitNumberComponent.setValidatorsForCheckDigit(true);
                        this.unitNumberComponent.focusOnCheckDigit();
                        return EMPTY;
                    }
                    this.validateUnitNumber(unitNumber);
                },
                error: (error) => {
                    this.showMessage(MessageType.ERROR, error.message);
                }
            });
        } else {
            this.validateUnitNumber(unitNumber);
        }
    }

    private validateUnitNumber(unitNumber: string) {
        if (unitNumber) {
            this.products.filter(p =>
                p.unitNumber === unitNumber
            ).forEach(p => {
                p.disabled = false;
            });
        }
        this.unitNumberComponent.reset();
    }

    private populateIrradiationBatch(irradiationProducts: IrradiationProductDTO[]) {

        irradiationProducts.forEach((product) => {
            this.addProductToList(product);
        });
    }

    private findIconsByProductFamily(productFamily: string) {
        return this._productIconService.getIconByProductFamily(productFamily);
    }

    private getStatuses(status: string) {
        let statuses = [];
        if (status) {
            statuses.push({
                value: status,
                classes: this.statusToColorClass(status),
            });
        }
        statuses.push(
            {
                value: PENDING_INSPECTION,
                classes: this.statusToColorClass(PENDING_INSPECTION),
            },
        )
        return statuses;
    }

    private statusToColorClass(status: string) {
        switch (status) {
            case QUARANTINED:
                return 'bg-orange-500 text-white';
            case AVAILABLE:
                return 'bg-green-500 text-white';
            case EXPIRED:
            case UNSUITABLE:
            case DISCARDED:
                return 'bg-red-500 text-white';
            default:
                return 'bg-gray-500 text-white';
        }
    }

    private getFinalStatus(inventory: IrradiationProductDTO) {
        if (inventory.status === DISCARDED) {
            return DISCARDED;
        }
        if (inventory.expired) {
            return EXPIRED;
        }
        if (inventory.unsuitableReason) {
            return UNSUITABLE;
        }
        if (inventory.quarantines && inventory.quarantines.length !==0) {
            return QUARANTINED;
        }
        return AVAILABLE;
    }

    private addProductToList(newProduct: IrradiationProductDTO) {
        newProduct.disabled = true;
        this.initialProductsState.push({...newProduct});
        this.products.push(newProduct);
        this.products = this.products.sort((productA, productB) => {
            return productB.order - productA.order;
        });

        this.unitNumberComponent.reset();
        this.unitNumberComponent.focusOnUnitNumber();

        this.allProducts.push({...newProduct});
    }

    private notInProductList(product: ValidationDataDTO) {
        return !this.products.find(
            (p) =>
                p.productCode === product.productCode &&
                p.unitNumber === product.unitNumber
        );
    }

    get numberOfUnits() {
        return this.products
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
        const enabledProducts = this.products.filter(p => !p.disabled);
        if (this.selectedProducts.length === enabledProducts.length) {
            this.selectedProducts = [];
        } else {
            this.selectedProducts = [].concat(enabledProducts);
        }
    }

    openRecordVisualInspectionModal() {
        const dialogRef = this.matDialog.open<
            RecordVisualInspectionModalComponent,
            null,
            RecordVisualInpectionResult
        >(RecordVisualInspectionModalComponent, {
            disableClose: true,
            width: '30rem',
        });

        dialogRef
            .afterClosed()
            .subscribe(({ irradiated }) => {
                const statuses = [PENDING_INSPECTION, IRRADIATED, NOT_IRRADIATED];
                this.selectedProducts = this.selectedProducts
                    .map(product => {
                        product.statuses = product.statuses.map(status => {
                            if (statuses.includes(status.value)) {
                                status.value = irradiated ? IRRADIATED : NOT_IRRADIATED;
                            }
                            return status;
                        });
                        return product;
                    });
                }
            );
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

    loadIrradiationId(irradiationId: string) {
        this.deviceId = true;
        const irradiationProducts: IrradiationProductDTO[] = [
            {
                unitNumber: "W036825314134",
                productCode: 'E468900',
                productDescription: 'WHOLE BLOOD|CPD/500mL/refg|ResLeu:<5E6',
                status: 'AVAILABLE',
                productFamily: 'WHOLE_BLOOD',
                icon: this.findIconsByProductFamily('WHOLE_BLOOD'),
                order: 1,
                statuses: this.getStatuses(AVAILABLE),
                location: '',
                comments: '',
                statusReason: '',
                unsuitableReason: '',
                expired: false,
                alreadyIrradiated: false,
                notConfigurableForIrradiation: false,
                quarantines: null
            },
            {
                unitNumber: "W036825314134",
                productCode: 'E468800',
                productDescription: 'FRESH FROZEN PLASMA|CPD/XX/<=-18C',
                status: 'QUARANTINED',
                productFamily: 'WHOLE_BLOOD',
                icon: this.findIconsByProductFamily('WHOLE_BLOOD'),
                order: 1,
                statuses: this.getStatuses(QUARANTINED),
                location: '',
                comments: '',
                statusReason: '',
                unsuitableReason: '',
                expired: false,
                alreadyIrradiated: false,
                notConfigurableForIrradiation: false,
                quarantines: null
            },
            {
                unitNumber: "W036825314135",
                productCode: 'E469900',
                productDescription: 'WHOLE BLOOD|CPD/500mL/refg|ResLeu:<5E6',
                status: 'AVAILABLE',
                productFamily: 'WHOLE_BLOOD',
                icon: this.findIconsByProductFamily('WHOLE_BLOOD'),
                order: 1,
                statuses: this.getStatuses(QUARANTINED),
                location: '',
                comments: '',
                statusReason: '',
                unsuitableReason: '',
                expired: false,
                alreadyIrradiated: false,
                notConfigurableForIrradiation: false,
                quarantines: null
            },
        ];

        this.populateIrradiationBatch(irradiationProducts);
        this.unitNumberComponent.controlUnitNumber.enable();
        setTimeout(() => this.unitNumberComponent.focusOnUnitNumber(), 0);
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

}
