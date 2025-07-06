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

const AVAILABLE = 'AVAILABLE';
const DISCARDED = 'DISCARDED';
const QUARANTINED = 'QUARANTINED';
const UNSUITABLE = 'UNSUITABLE';
const EXPIRED = 'EXPIRED';

@Component({
  host: {
      class: 'flex flex-1 h-full',
  },
  selector: 'app-start-irradiation',
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

    constructor(
        private readonly router: Router,
        private readonly formBuilder: FormBuilder,
        private readonly processHeaderService: ProcessHeaderService,
        private readonly irradiationService: IrradiationService,
        private readonly confirmationService: FuseConfirmationService,
        private readonly toaster: ToastrService,
        private readonly facilityService: FacilityService,
        private readonly activatedRoute: ActivatedRoute,
        private readonly matDialog: MatDialog
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
        setTimeout(() => this.unitNumberComponent.form.enable());
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

    validateUnit(event: ValidateUnitEvent) {
       console.log('validateUnit', event);

        const irradiationProducts: IrradiationProductDTO[] = [
            {
                unitNumber: "W036825314134",
                productCode: 'E468900',
                productDescription: 'WHOLE BLOOD|CPD/500mL/refg|ResLeu:<5E6',
                status: 'AVAILABLE',
                productFamily: 'WHOLE_BLOOD',
                icon: this.findIconsByProductFamily('WHOLE_BLOOD'),
                order: 1,
                statuses: [
                    {
                        value: 'AVAILABLE',
                        classes: this.statusToColorClass('AVAILABLE'),
                    },
                ],
            },
            {
                unitNumber: "W036825314134",
                productCode: 'E468800',
                productDescription: 'FRESH FROZEN PLASMA|CPD/XX/<=-18C',
                status: 'QUARANTINED',
                productFamily: 'WHOLE_BLOOD',
                icon: this.findIconsByProductFamily('WHOLE_BLOOD'),
                order: 1,
                statuses: [
                    {
                        value: 'QUARANTINED',
                        classes: this.statusToColorClass('QUARANTINED'),
                    },
                ],
            },
        ];

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
                    this.populateCentrifugationBatch(selectedOption);
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
            case QUARANTINED:
                return 'bg-orange-500 text-white';
            case AVAILABLE:
                return 'bg-green-500 text-white';
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
        return this.allProducts.length;
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
            this.selectedProducts = [].concat(this.products);
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

    loadIrradiationId(irradiationId: string) {
        console.log('irradiationId', irradiationId);
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
