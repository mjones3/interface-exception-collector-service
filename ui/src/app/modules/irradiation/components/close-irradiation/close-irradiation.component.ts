import {
    AfterViewInit,
    ChangeDetectorRef,
    Component,
    effect,
    inject,
    Input,
    OnInit,
    TemplateRef,
    ViewChild, ViewEncapsulation
} from '@angular/core';
import {ActionButtonComponent} from "../../../../shared/components/buttons/action-button.component";
import {FuseCardComponent} from "../../../../../@fuse";
import {InputComponent} from "../../../../shared/components/input/input.component";
import {MatDivider} from "@angular/material/divider";
import {
    FacilityService,
    ProcessHeaderComponent,
    ProcessHeaderService,
    ScanUnitNumberCheckDigitComponent
} from "@shared";
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {UnitNumberCardComponent} from "../../../../shared/components/unit-number-card/unit-number-card.component";
import {ProductIconsService} from "../../../../shared/services/product-icon.service";
import {
    ConsequenceType,
    IrradiationProductDTO,
    MessageType, ReasonDTO,
    RecordVisualInpectionResult,
    ValidateUnitEvent,
    ValidationDataDTO
} from "../../models/model";
import {ActivatedRoute, Router} from "@angular/router";
import {IrradiationService} from "../../services/irradiation.service";
import {FuseConfirmationService} from "../../../../../@fuse/services/confirmation";
import {ToastrService} from "ngx-toastr";
import {MatDialog} from "@angular/material/dialog";
import {IrradiationSelectProductModal} from "../start-irradiation/select-product-modal/select-product-modal.component";
import {NgStyle} from "@angular/common";
import {
    RecordVisualInspectionModalComponent
} from "../record-visual-inspection-modal/record-visual-inspection-modal.component";

const AVAILABLE = 'AVAILABLE';
const QUARANTINED = 'QUARANTINED';
const PENDING_INSPECTION = 'PENDING INSPECTION';
const EXPIRED = 'EXPIRED';
const IRRADIATED = 'IRRADIATED';
const NOT_IRRADIATED = 'NOT IRRADIATED';

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
    isCheckDigitVisible = true;
    numOfMaxUnits = 0;
    selectedFilter = 'all';
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
        private readonly changeDetectorRef: ChangeDetectorRef
    ) {
        effect(() => {
            this.processHeaderService.setActions(this.buttons);
        });

        this.form = this.formBuilder.group({
            irradiationId: [null, [Validators.required]],
            lotNumber: [null, [Validators.required]]
        });
    }

    ngOnInit() {
        this.isCheckDigitVisible = (
            this.activatedRoute.snapshot.data as { useCheckDigit: boolean }
        )?.useCheckDigit;
    }

    ngAfterViewInit(): void {
        setTimeout(() => this.unitNumberComponent.form.disable());
    }

    get irradiation() {
        return this.form.get('irradiationId');
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
        this.unitNumberComponent.reset();
        this.redirect();
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
        this.irradiationService
            .submitCentrifugationBatch(requestDTO)
            .subscribe((response) => {
                if (response.errors && response.errors.length > 0) {
                    //this.irradiationService.handleErrors(response);
                } else {
                    this.showMessage(
                        MessageType.SUCCESS,
                        'Start irradiation successfully completed'
                    );
                    this.redirect();
                }
            });
    }


    validateUnit(event: { unitNumber: string }) {
        console.log('validateUnit', event);
        const unitNumber = event.unitNumber;
        if (unitNumber) {
            this.products.filter(p =>
                p.unitNumber === unitNumber
            ).forEach(p => {
                p.disabled = false;
            });
        }
        this.unitNumberComponent.reset();
    }

    private populateCentrifugationBatch(irradiationProducts: IrradiationProductDTO[]) {

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
                return 'bg-red-500 text-white';
            default:
                return 'bg-gray-500 text-white';
        }
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
            .subscribe(({successful, comment, reasons}) =>
                    console.log('dialog closed', successful, comment, reasons)
                // this.applyVisualInspectionOnSelectedProducts(
                //     successful,
                //     reasons,
                //     comment
                // )
            );
    }

    private applyVisualInspectionOnSelectedProducts(
        successful: boolean,
        reasons: ReasonDTO[],
        comments: string
    ) {
        if (successful !== undefined && reasons) {
            this.selectedProducts
                .filter((product) => !product.visualInspection)
                .forEach((product) => {
                    const consequence = reasons
                        .sort(
                            (a: ReasonDTO, b: ReasonDTO) =>
                                a.priority - b.priority
                        )
                        .map((r: ReasonDTO) => r.consequenceType)[0];

                    this.overrideProductStatus(
                        product,
                        successful,
                        consequence
                    );

                    if (successful) {
                        product.statuses.push({
                            value: IRRADIATED,
                            classes: 'bg-green-500 text-white',
                        });
                    } else {
                        product.statuses.push({
                            value: NOT_IRRADIATED,
                            classes: 'bg-gray-200 text-black',
                        });
                    }

                    product.visualInspection = {
                        reasons: reasons.map(
                            ({reasonKey, consequenceType, priority}) =>
                                ({
                                    reasonKey,
                                    consequenceType,
                                    priority,
                                }) as ReasonDTO
                        ),
                        status: successful ? IRRADIATED : NOT_IRRADIATED,
                        comments,
                    };
                    this.selectedProducts = [];
                    this.changeDetectorRef.detectChanges();
                });

            this.applyFilter();
        }
    }

    private applyFilter() {
        this.selectedProducts = [];
        this.products = this.allProducts.filter((p) => {
            if (this.selectedFilter === 'all') {
                return true;
            } else if (this.selectedFilter === 'complete') {
                return !!p.visualInspection;
            } else {
                return !p.visualInspection;
            }
        });

        this.products = this.products.sort((a, b) => {
            return b.order - a.order;
        });
    }

    private overrideProductStatus(
        product: IrradiationProductDTO,
        successful: boolean,
        consequence: string
    ) {
        product.statuses = [];
        const selectedProduct = this.initialProductsState.find(
            (p) =>
                p.productCode === product.productCode &&
                p.unitNumber === product.unitNumber
        );

        if (!successful && selectedProduct.status !== 'DISCARD') {
            this.overrideStatus(product, consequence);
        } else {
            if (selectedProduct) {
                this.overrideStatus(product, selectedProduct.status);
            }
        }
    }

    private overrideStatus(product: IrradiationProductDTO, status: string): void {
        product.status = status;
        product.statusClasses = this.retrieveStatusClass(status);
    }

    private retrieveStatusClass(status: string): 'error' | 'warning' | '' {
        if (status === ConsequenceType.DISCARD) {
            return 'error';
        } else if (status === ConsequenceType.QUARANTINE) {
            return 'warning';
        }

        return '';
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
        const irradiationProducts: IrradiationProductDTO[] = [
            {
                unitNumber: "W036825314134",
                productCode: 'E468900',
                productDescription: 'WHOLE BLOOD|CPD/500mL/refg|ResLeu:<5E6',
                status: 'AVAILABLE',
                productFamily: 'WHOLE_BLOOD',
                icon: this.findIconsByProductFamily('WHOLE_BLOOD'),
                order: 1,
                statuses: this.getStatuses(AVAILABLE)
            },
            {
                unitNumber: "W036825314134",
                productCode: 'E468800',
                productDescription: 'FRESH FROZEN PLASMA|CPD/XX/<=-18C',
                status: 'QUARANTINED',
                productFamily: 'WHOLE_BLOOD',
                icon: this.findIconsByProductFamily('WHOLE_BLOOD'),
                order: 1,
                statuses: this.getStatuses(QUARANTINED)
            },
            {
                unitNumber: "W036825314135",
                productCode: 'E469900',
                productDescription: 'WHOLE BLOOD|CPD/500mL/refg|ResLeu:<5E6',
                status: 'AVAILABLE',
                productFamily: 'WHOLE_BLOOD',
                icon: this.findIconsByProductFamily('WHOLE_BLOOD'),
                order: 1,
                statuses: this.getStatuses(QUARANTINED)
            },
        ];

        this.populateCentrifugationBatch(irradiationProducts);
        this.unitNumberComponent.form.enable()
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

}
