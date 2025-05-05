import { Component, OnInit, signal, ViewChild } from '@angular/core';
import { RecoveredPlasmaShipmentCommon } from '../../recovered-plasma-shipment.common';
import { AsyncPipe, CommonModule } from '@angular/common';
import { MatDividerModule } from '@angular/material/divider';
import { ProcessHeaderComponent, ProcessHeaderService, ScanUnitNumberCheckDigitComponent, ToastrImplService } from '@shared';
import { ActionButtonComponent } from 'app/shared/components/buttons/action-button.component';
import { ScanUnitNumberProductCodeComponent } from 'app/shared/components/scan-unit-number-product-code/scan-unit-number-product-code.component';
import { UnitNumberCardComponent } from 'app/shared/components/unit-number-card/unit-number-card.component';
import { ShippingCartonInformationCardComponent } from '../../shared/shipping-carton-information-card/shipping-carton-information-card.component';
import { ShippingInformationCardComponent } from '../../shared/shipping-information-card/shipping-information-card.component';
import { FormBuilder, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { Store } from '@ngrx/store';
import { ProductIconsService } from 'app/shared/services/product-icon.service';
import { CookieService } from 'ngx-cookie-service';
import { RecoveredPlasmaService } from '../../services/recovered-plasma.service';
import { catchError, map, Observable, switchMap, take, tap } from 'rxjs';
import { CartonDTO } from '../../models/recovered-plasma.dto';
import { ApolloError } from '@apollo/client/errors';
import handleApolloError from 'app/shared/utils/apollo-error-handling';
import { consumeUseCaseNotifications } from 'app/shared/utils/notification.handling';
import { VerifyRecoveredPlasmaProductsComponent } from '../verify-recovered-plasma-products/verify-recovered-plasma-products.component';
import { MatButtonModule } from '@angular/material/button';
import { MatStep, MatStepper, MatStepperModule } from '@angular/material/stepper';
import { MatFormFieldModule } from '@angular/material/form-field';
import { FuseCardComponent } from '@fuse/components/card/public-api';
import { BasicButtonComponent } from 'app/shared/components/buttons/basic-button.component';
import { FuseAlertType } from '@fuse/components/alert/public-api';
import { GlobalMessageComponent } from 'app/shared/components/global-message/global-message.component';
import { PackCartonItemsDTO } from '../../graphql/mutation-definitions/pack-items.graphql';
import { ERROR_MESSAGE } from 'app/core/data/common-labels';
import { UseCaseNotificationDTO } from 'app/shared/models/use-case-response.dto';
import { VerifyCartonItemsDTO } from '../../graphql/mutation-definitions/verify-products.graphql';
import { AddRecoveredPlasmaProductsComponent } from '../add-recovered-plasma-products/add-recovered-plasma-products.component';
import { CloseCartonDTO } from '../../graphql/mutation-definitions/close-carton.graphql';

@Component({
  selector: 'biopro-manage-carton-products',
  standalone: true,
  imports: [
        AsyncPipe,
        ProcessHeaderComponent,
        CommonModule,
        MatDividerModule,
        ScanUnitNumberCheckDigitComponent,
        UnitNumberCardComponent,
        ShippingInformationCardComponent,
        ShippingCartonInformationCardComponent,
        ActionButtonComponent,
        FuseCardComponent,
        ScanUnitNumberProductCodeComponent,
        VerifyRecoveredPlasmaProductsComponent,
        MatButtonModule,
        MatStepperModule,
        FormsModule,
        ReactiveFormsModule,
        MatFormFieldModule,
        AddRecoveredPlasmaProductsComponent,
        BasicButtonComponent,
        GlobalMessageComponent,
        MatStep,
        MatStepper
  ],
  templateUrl: './manage-carton-products.component.html',
  styleUrl: './manage-carton-products.component.scss'
})
export class ManageCartonComponent extends RecoveredPlasmaShipmentCommon
implements OnInit {
  isLinear: boolean = true;
  messageSignal = signal<string>(null);
  messageTypeSignal = signal<FuseAlertType>(null);
  cartonDetailsSignal = signal<CartonDTO>(null);
  @ViewChild('verifyProductsControl') verifyProductsControl: VerifyRecoveredPlasmaProductsComponent;
  @ViewChild('addProductsControl') addProductsControl: AddRecoveredPlasmaProductsComponent;
  @ViewChild('stepper') stepper: MatStepper;
  constructor(
    public header: ProcessHeaderService,
    protected fb: FormBuilder,
    protected router: Router,
    protected route: ActivatedRoute,
    protected store: Store,
    protected toastr: ToastrImplService,
    protected productIconService: ProductIconsService,
    protected recoveredPlasmaService: RecoveredPlasmaService,
    protected cookieService: CookieService,
  ){
    super(
      route,
      router,
      store,
      recoveredPlasmaService,
      toastr,
      productIconService,
      cookieService
  );
  }

ngOnInit(): void {
    this.loadRecoveredPlasmaShippingCartonDetails(this.routeIdComputed())
        .pipe(
            switchMap((carton) =>
                this.loadRecoveredPlasmaShippingDetails(carton.shipmentId)
            )
        ).pipe(
            tap(() => {
               this.goToStep();
            })
        )
    .subscribe();
}

goToStep(): void {
    this.route.data.pipe(take(1)).subscribe(data => {
        if (data && data['step']) {
            this.stepper.selectedIndex = data['step'];
        }
    });
}

loadRecoveredPlasmaShippingCartonDetails(
    id: number
): Observable<CartonDTO> {
    return this.recoveredPlasmaService.getCartonById(id).pipe(
        catchError((error: ApolloError) => {
            handleApolloError(this.toastr, error);
        }),
        tap((response) =>
            consumeUseCaseNotifications(
                this.toastr,
                response.data?.findCartonById?.notifications
            )
        ),
        map((response) => {
            const { data } = response.data.findCartonById;
            this.setCartonDetails(data);
            return data;
        })
    );
}

setCartonDetails(data: CartonDTO){
    this.cartonDetailsSignal.set(data);
}

onClickPrevious(data) {
    this.loadRecoveredPlasmaShippingCartonDetails(this.cartonDetailsSignal().id)
        .subscribe({
            next: (cartonDetails) => {
                this.stepper.selectedIndex = data.index;
                if (data.displayStaticMessage) {
                    this.messageTypeSignal.set('warning');
                    this.messageSignal.set(data.resetMessage);
                }
                this.addProductsControl.disableInputsIfMaxCartonProduct(cartonDetails);
            }
        });
}

getValuesForReset(data){
    const urlLink = data.next;
    const url = new URL(urlLink, window.location.origin);
    const params = new URLSearchParams(url.search);
    const index = parseInt(params.get('step') || '0');
    const displayStaticMessage = params.get('reset');
    const resetMessage = params.get('resetMessage');
    return {
        index,
        displayStaticMessage,
        resetMessage
    }
}

onClickNext(){
    this.loadRecoveredPlasmaShippingCartonDetails(this.cartonDetailsSignal().id)
    .subscribe();
}

navigateBackToShipmentDetails() {
    this.router.navigateByUrl(
        `/recovered-plasma/${this.shipmentDetailsSignal().id}/shipment-details`
    );
}

// Add Carton Products
enterAndVerifyProduct(item: PackCartonItemsDTO) {
    return this.recoveredPlasmaService
        .addCartonProducts(this.getCartonProductRequest(item))
        .pipe(
            catchError((err) => {
                this.resetAddProductGroup();
                this.toastr.error(ERROR_MESSAGE);
                throw err;
            })
        )
        .subscribe({
            next: (response) => {
                const productResult = response?.data?.packCartonItem;
                const notifications: UseCaseNotificationDTO[] =
                    productResult.notifications;
                if (
                    productResult?.data &&
                    notifications[0].type === 'SUCCESS'
                ) {
                    this.setCartonDetails(productResult.data);
                    this.resetAddProductGroup();
                    this.AddProductFocusOnUnitNumber();
                } else {
                    if (notifications.length > 0) {
                        this.resetAddProductGroup();
                        if (notifications[0].type === 'INFO') {
                            const inventory = productResult?.data?.failedCartonItem;
                            this.recoveredPlasmaService.handleInfoNotificationAndDiscard(
                                notifications[0],
                                inventory,
                                this.locationCodeComputed(),
                                this.employeeIdSignal(),
                                this.getCallBacks()
                            );
                        } else {
                            this.recoveredPlasmaService.displayNotificationMessage(
                                notifications,
                                this.AddProductFocusOnUnitNumber.bind(this)
                            );
                            const notification = notifications[0];
                            if (
                                notification.name ===
                                'MAXIMUM_UNITS_BY_CARTON'
                            ) {
                                this.addProductsControl.disableInputsIfMaxCartonProduct(productResult.data);
                            } else {
                                this.resetAddProductGroup();
                            }
                        }
                    }
                }
            },
        });
}

private getCartonProductRequest(
  item: PackCartonItemsDTO
): PackCartonItemsDTO {
  return {
      cartonId: this.routeIdComputed(),
      unitNumber: item.unitNumber,
      productCode: item.productCode,
      locationCode: this.locationCodeComputed(),
      employeeId: this.employeeIdSignal(),
  };
}

resetAddProductGroup() {
  this.addProductsControl?.resetProductGroup();
}

AddProductFocusOnUnitNumber() {
  this.addProductsControl?.focusOnUnitNumber();
}

getCallBacks(){
  return {
     resetFn: this.resetAddProductGroup.bind(this),
     focusFn: this.AddProductFocusOnUnitNumber.bind(this)
  }
}


// Verify Carton Products
verifyProducts(item: VerifyCartonItemsDTO) {
  return this.recoveredPlasmaService
      .verifyCartonProducts(this.getVerifyProductRequest(item))
      .pipe(
          catchError((err) => {
              this.verifyProductsControl.resetProductGroup();
              this.toastr.error(ERROR_MESSAGE);
              throw err;
          })
      )
      .subscribe({
          next: (response) => {
              const productResult = response?.data?.verifyCarton;
              const notifications: UseCaseNotificationDTO[] =
                  productResult.notifications;
              if (
                  productResult?.data &&
                  notifications[0].type === 'SUCCESS'
              ) {
                this.setCartonDetails(productResult.data);
                  this.verifyProductsControl.resetProductGroup();
                  this.verifyProductsControl.focusOnUnitNumber();
              } else {
                  if (notifications.length > 0) {
                      this.verifyProductsControl.resetProductGroup();
                      if (notifications[0].type === 'INFO') {
                          const inventory = productResult?.data?.failedCartonItem;
                          this.recoveredPlasmaService.handleInfoNotificationAndDiscard(
                              notifications[0],
                              inventory,
                              this.locationCodeComputed(),
                              this.employeeIdSignal(),
                              this.getVerifyProductsCallBacks()
                          );
                      } else {
                        this.recoveredPlasmaService.displayNotificationMessage(
                            notifications,
                            this.verifyProductsControl.resetProductGroup.bind(this)
                            );
                          this.verifyProductsControl.resetProductGroup();
                      }
                  }
                  if(productResult._links !== null){
                      const nextLink = productResult._links;
                      const data = this.getValuesForReset(nextLink);
                      this.onClickPrevious(data);
                  }
              }
          },
      });
}

getVerifyProductsCallBacks(){
    return {
       resetFn: this.verifyProductsControl.resetProductGroup.bind(this),
       focusFn: this.verifyProductsControl.focusOnUnitNumber.bind(this)
    }
  }

private getVerifyProductRequest(
  item: VerifyCartonItemsDTO
): VerifyCartonItemsDTO {
  return {
      cartonId: this.routeIdComputed(),
      unitNumber: item.unitNumber,
      productCode: item.productCode,
      locationCode: this.locationCodeComputed(),
      employeeId: this.employeeIdSignal(),
  };
}


// Close Carton
closeCarton() {
  this.recoveredPlasmaService
      .closeCarton(this.closeCartonRequest())
      .pipe(
        catchError((error: ApolloError) => {
            handleApolloError(this.toastr, error);
        }),
        tap((response) =>
            consumeUseCaseNotifications(
                this.toastr,
                response.data?.closeCarton?.notifications
            )
        )
      )
      .subscribe((carton) => {
        const nextUrl = carton?.data?.closeCarton?._links.next;
        if (nextUrl) {
            this.router.navigateByUrl(nextUrl);
        }
    });
}

private closeCartonRequest(): CloseCartonDTO {
    return {
        cartonId: this.routeIdComputed(),
        locationCode: this.locationCodeComputed(),
        employeeId: this.employeeIdSignal(),
    };
  }
}
