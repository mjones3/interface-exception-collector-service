import {AfterViewInit, Component, effect, inject, OnInit, TemplateRef, ViewChild, ViewEncapsulation} from '@angular/core';
import {ActionButtonComponent} from "../../../../shared/components/buttons/action-button.component";
import {FuseAlertType, FuseCardComponent} from "../../../../../@fuse";
import {MatDivider} from "@angular/material/divider";
import {UnitNumberCardComponent} from "../../../../shared/components/unit-number-card/unit-number-card.component";
import {
    FacilityService,
    ProcessHeaderComponent,
    ProcessHeaderService,
    ScanUnitNumberCheckDigitComponent
} from "@shared";
import {InputComponent} from "../../../../shared/components/input/input.component";
import {FormBuilder, FormGroup} from "@angular/forms";
import {ActivatedRoute, Router} from "@angular/router";
import {ToastrService} from "ngx-toastr";
import {
    CentrifugationProductDTO,
    CheckDigitResponseDTO, DeviceDTO,
    MessageType, ProductDataDTO,
    UnitNumberResponseDTO,
    ValidateUnitEvent, ValidationDataDTO
} from "../../models/model";
import {of} from "rxjs";
import { switchMap } from 'rxjs/operators';
import {ProductIconsService} from "../../../../shared/services/product-icon.service";
import {FuseConfirmationService} from "../../../../../@fuse/services/confirmation";
import {IrradiationService} from "../../services/irradiation.service";
import {AsyncPipe, NgStyle} from "@angular/common";

const AVAILABLE = 'AVAILABLE';
const DISCARDED = 'DISCARDED';
const QUARANTINED = 'QUARANTINED';
const UNSUITABLE = 'UNSUITABLE';
const EXPIRED = 'EXPIRED';
const CENTRIFUGE = 'CENTRIFUGE';

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
        AsyncPipe,
        ProcessHeaderComponent
    ],
  templateUrl: './start-irradiation.component.html',
  styleUrl: './start-irradiation.component.scss',
  encapsulation: ViewEncapsulation.None,
})
export class StartIrradiationComponent implements OnInit, AfterViewInit {

    protected readonly messageType: FuseAlertType = 'warning';
    private readonly _productIconService = inject(ProductIconsService);
    isCheckDigitVisible = true;
    numOfMaxUnits = 0;
    selectedProducts: CentrifugationProductDTO[] = [];
    products: CentrifugationProductDTO[] = [];
    initialProductsState: CentrifugationProductDTO[] = [];
    allProducts: CentrifugationProductDTO[] = [];
    deviceId: string;

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
        private readonly activatedRoute: ActivatedRoute
    ) {
        effect(() => {
            this.processHeaderService.setActions(this.buttons);
        });

        this.form = this.formBuilder.group({
            centrifugationId: [null, []],
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

    get centrifugation() {
        return this.form.get('centrifugationId');
    }

    openCancelConfirmationDialog(): void {
        const dialogRef = this.confirmationService.open({
            title: 'Confirmation',
            message:
                'Products added will be removed from the list without finishing the Centrifugation process. Are you sure you want to continue?',
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
        return this.numberOfUnits > 0;
    }

    get disableCancelButton() {
        return !this.deviceId;
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
                        'Centrifugation complete.'
                    );
                    this.redirect();
                }
            });
    }

    validateUnit(event: ValidateUnitEvent) {
        const location = this.facilityService.getFacilityCode();
        const { unitNumber, checkDigit, scanner } = event;
        if (this.products.length >= this.numOfMaxUnits) {
            this.showMessage(
                MessageType.ERROR,
                'Maximum number of units reached'
            );

            return;
        }

        // const $checkDigitVerification =
        //     this.isCheckDigitVisible && !scanner
        //         ? this.wholeBloodService.validateCheckDigit(
        //             unitNumber,
        //             checkDigit
        //         )
        //         : of(null);

        // $checkDigitVerification
        //     .pipe(
        //         switchMap((response) => {
        //             const skipCheckDigit = !response;
        //             if (!skipCheckDigit) {
        //                 return this.checkDigitFieldError(response.data).pipe(
        //                     switchMap((res) => {
        //                         this.wholeBloodService.handleErrors(response);
        //                         if (res) {
        //                             return this.irradiationService.scanOrEnterUnitNumber(
        //                                 { unitNumber, location }
        //                             );
        //                         }
        //                         return of(null);
        //                     })
        //                 );
        //             }
        //             return this.irradiationService.scanOrEnterUnitNumber({
        //                 unitNumber,
        //                 location,
        //             });
        //         }),
        //         switchMap((response) => {
        //             const discardError = response?.errors?.find(
        //                 (error) => error.extensions?.isMarkedForDiscard
        //             );
        //             if (discardError) {
        //                 this.discardProduct(unitNumber, discardError.message);
        //                 this.unitNumberComponent.reset();
        //                 return of(null);
        //             }
        //             if (response.errors && response.errors.length > 0) {
        //                 this.unitNumberComponent.reset();
        //                 this.irradiationService.handleErrors(response);
        //                 return of(null);
        //             } else {
        //                 return of(response.data);
        //             }
        //         })
        //     )
        //     .subscribe((productData) => {
        //         if (productData) {
        //             this.populateCentrifugationBatch(productData);
        //         }
        //     });
    }

    private discardProduct(unitNumber: string, discardMessage: string) {
        // this.wholeBloodService
        //     .donationInformation(unitNumber, null)
        //     .pipe(
        //         switchMap((donationResponse) => {
        //             this.wholeBloodService.handleErrors(donationResponse);
        //             const { inventory, donation } =
        //                 donationResponse.data.donationInformation;
        //             const discardRequestDTO = {
        //                 unitNumber: donation.unitNumber,
        //                 productCode: inventory.productCode,
        //                 productShortDescription: inventory.productCode,
        //                 productFamily: inventory.productFamily,
        //                 locationCode: inventory.currentLocation,
        //                 reasonDescriptionKey: inventory.discardReason,
        //                 employeeId: '4c973896-5761-41fc-8217-07c5d13a004b', // TODO get employeeId
        //                 triggeredBy: 'WHOLE_BLOOD',
        //                 comments: '',
        //             };
        //             return this.discardService.discardProduct(
        //                 discardRequestDTO
        //             );
        //         })
        //     )
        //     .subscribe((response) => {
        //         this.discardService.handleErrors(response);
        //         if (response) {
        //             this.openDiscardDialog(discardMessage);
        //         }
        //     });
    }

    openDiscardDialog(message: string): void {
        this.confirmationService.open({
            title: 'Discarded',
            message,
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
                    show: false,
                },
            },
        });
    }

    private checkDigitFieldError(response: CheckDigitResponseDTO) {
        if (response && !response.checkDigit.isValid) {
            //this.unitNumberComponent.setErrorCheckDigit({ invalid: true });
            this.unitNumberComponent.form.controls['checkDigit']?.setErrors({
                invalid: true,
            });
            this.unitNumberComponent.focusOnCheckDigit();
            return of(null);
        }
        return of('valid');
    }

    private populateCentrifugationBatch(productData: UnitNumberResponseDTO) {
        const product =
            productData.enterUnitNumberForCentrifugation as ProductDataDTO;
        const centrifugationProducts: CentrifugationProductDTO[] = [
            {
                unitNumber: product.unitNumber,
                productCode: product.productCode,
                productName: product.productName,
                status: product.status,
                productFamily: product.productFamily,
                icon: this.findIconsByProductFamily(product.productFamily),
                order: 1,
                statuses: [
                    {
                        value: product.status,
                        classes: this.statusToColorClass(product.status),
                    },
                ],
            },
        ];

        const notAddedProducts = centrifugationProducts.filter((p) =>
            this.notInProductList(p)
        );

        if (notAddedProducts.length === 0) {
            this.showMessage(
                MessageType.WARNING,
                'Unit has already been added to the list'
            );
            return;
        }

        centrifugationProducts.forEach((product) => {
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
            case UNSUITABLE:
            case DISCARDED:
            case EXPIRED:
                return 'bg-red-500 text-white';
        }
    }

    private addProductToList(newProduct: CentrifugationProductDTO) {
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
                'All changes will be removed without finishing the centrifugation process. Are you sure you want to continue?',
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

    toggleProduct(product: CentrifugationProductDTO) {
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

    loadCentrifugationId(centrifugeId: string) {
        const deviceDtoRequest: DeviceDTO = {
            location: this.facilityService.getFacilityCode(),
            bloodCenterId: centrifugeId,
            deviceType: CENTRIFUGE,
        };

        this.irradiationService
            .loadDeviceById(deviceDtoRequest)
            .subscribe((result) => {
                if (result.errors && result.errors.length > 0) {
                    this.showMessage(
                        MessageType.ERROR,
                        result.errors[0].message
                    );
                } else {
                    this.numOfMaxUnits = result.data.enterDeviceId.maxProducts
                        ? result.data.enterDeviceId.maxProducts
                        : 0;
                    this.deviceId = result.data.enterDeviceId.bloodCenterId;
                    this.centrifugation.disable();
                    this.unitNumberComponent.controlUnitNumber.enable();
                    this.unitNumberComponent.focusOnUnitNumber();
                }
            });
    }

    redirect() {
        this.router.navigateByUrl('whole-blood/centrifugation');
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
