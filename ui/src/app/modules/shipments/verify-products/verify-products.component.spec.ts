import { ComponentFixture, fakeAsync, TestBed } from '@angular/core/testing';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { MatDialogModule } from '@angular/material/dialog';
import { MatIconTestingModule } from '@angular/material/icon/testing';
import { By } from '@angular/platform-browser';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { provideMockStore } from '@ngrx/store/testing';
import { TranslateModule } from '@ngx-translate/core';
import { NotificationDto } from '@shared';
import { ApolloModule, MutationResult } from 'apollo-angular';
import { ApolloTestingModule } from 'apollo-angular/testing';
import { ToastrModule, ToastrService } from 'ngx-toastr';
import { of } from 'rxjs';
import { FuseConfirmationService } from '../../../../@fuse/services/confirmation';
import { RuleResponseDTO } from '../../../shared/models/rule.model';
import { ShipmentService } from '../services/shipment.service';
import { VerifyProductsComponent } from './verify-products.component';
import { AuthState } from 'app/core/state/auth/auth.reducer';

const SHIPMENT_ID = 1;
describe('VerifyProductsComponent', () => {
    let component: VerifyProductsComponent;
    let fixture: ComponentFixture<VerifyProductsComponent>;
    let shipmentService: ShipmentService;
    let fuseConfirmationService: FuseConfirmationService;
    let toastr: ToastrService;
    let router: Router;
    
    const initialState: AuthState = {
        id: 'mock-user-id',
        loaded: true,
    };

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                VerifyProductsComponent,
                ApolloTestingModule,
                NoopAnimationsModule,
                MatIconTestingModule,
                ApolloModule,
                MatDialogModule,
                TranslateModule.forRoot(),
                ToastrModule.forRoot(),
            ],
            providers: [
                provideHttpClientTesting(),
                provideMockStore({initialState}),
                {
                    provide: ActivatedRoute,
                    useValue: {
                        snapshot: {
                            params: {
                                id: SHIPMENT_ID,
                            },
                        },
                    },
                },
                {
                    provide: Router,
                    useValue: {
                        navigateByUrl: jest.fn(),
                    },
                },
                ShipmentService,
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(VerifyProductsComponent);
        component = fixture.componentInstance;

        shipmentService = TestBed.inject(ShipmentService);
        fuseConfirmationService = TestBed.inject(FuseConfirmationService);
        toastr = TestBed.inject(ToastrService);
        router = TestBed.inject(Router);
        jest.spyOn(shipmentService, 'getShipmentById').mockReturnValue(of());
        jest.spyOn(
            shipmentService,
            'getShipmentVerificationDetailsById'
        ).mockReturnValue(of());
        jest.spyOn(
            shipmentService,
            'getNotificationDetailsByShipmentId'
        ).mockReturnValue(of());
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it("should cancel second verification directly when there's no verified products", fakeAsync(async () => {
        const cancelSecondVerificationSpy = jest
            .spyOn(shipmentService, 'cancelSecondVerification')
            .mockReturnValue(
                of<
                    MutationResult<{
                        cancelSecondVerification: RuleResponseDTO<never>;
                    }>
                >({
                    data: {
                        cancelSecondVerification: {
                            ruleCode: '200 OK',
                            notifications: [
                                { notificationType: 'SUCCESS' },
                            ] as NotificationDto[],
                            _links: {
                                next: `/shipment/${SHIPMENT_ID}/shipment-details`,
                            },
                        },
                    },
                })
            );
        const fuseConfirmationOpenDialogSpy = jest.spyOn(
            fuseConfirmationService,
            'open'
        );
        const routeSpy = jest.spyOn(router, 'navigateByUrl');
        const toastrSpy = jest.spyOn(toastr, 'show');

        const element = fixture.debugElement;
        const pageCancelButton = element.query(By.css('#cancelActionBtn'))
            .nativeElement as HTMLButtonElement;
        expect(pageCancelButton.disabled).toBeFalsy();
        pageCancelButton.click();
        fixture.detectChanges();

        expect(cancelSecondVerificationSpy).toHaveBeenCalled();
        expect(fuseConfirmationOpenDialogSpy).not.toHaveBeenCalled();
        expect(toastrSpy).toHaveBeenCalled();
        expect(routeSpy).toHaveBeenCalled();
    }));

    it("should open cancel dialog when there's a confirmation notification", fakeAsync(async () => {
        const cancelSecondVerificationSpy = jest
            .spyOn(shipmentService, 'cancelSecondVerification')
            .mockReturnValue(
                of<
                    MutationResult<{
                        cancelSecondVerification: RuleResponseDTO<never>;
                    }>
                >({
                    data: {
                        cancelSecondVerification: {
                            notifications: [
                                { notificationType: 'CONFIRMATION' },
                            ] as NotificationDto[],
                        },
                    },
                })
            );
        const fuseConfirmationOpenDialogSpy = jest.spyOn(
            fuseConfirmationService,
            'open'
        );
        const routeSpy = jest.spyOn(router, 'navigateByUrl');
        const toastrSpy = jest.spyOn(toastr, 'show');

        const element = fixture.debugElement;
        const pageCancelButton = element.query(By.css('#cancelActionBtn'))
            .nativeElement as HTMLButtonElement;
        expect(pageCancelButton.disabled).toBeFalsy();
        pageCancelButton.click();
        fixture.detectChanges();

        expect(cancelSecondVerificationSpy).toHaveBeenCalled();
        expect(fuseConfirmationOpenDialogSpy).toHaveBeenCalled();
        expect(toastrSpy).not.toHaveBeenCalled();
        expect(routeSpy).not.toHaveBeenCalled();
    }));

    it('should show an error message when rule code is not "200 OK"', fakeAsync(async () => {
        const cancelSecondVerificationSpy = jest
            .spyOn(shipmentService, 'cancelSecondVerification')
            .mockReturnValue(
                of<
                    MutationResult<{
                        cancelSecondVerification: RuleResponseDTO<never>;
                    }>
                >({
                    data: {
                        cancelSecondVerification: {
                            ruleCode: '400 BAD_REQUEST',
                            notifications: [
                                {
                                    notificationType: 'WARN',
                                    message:
                                        'Second Verification cannot be cancelled because contains product(s) that should be removed from the shipment.',
                                },
                            ] as NotificationDto[],
                        },
                    },
                })
            );
        const fuseConfirmationOpenDialogSpy = jest.spyOn(
            fuseConfirmationService,
            'open'
        );
        const routeSpy = jest.spyOn(router, 'navigateByUrl');
        const toastrSpy = jest.spyOn(toastr, 'show');

        const element = fixture.debugElement;
        const pageCancelButton = element.query(By.css('#cancelActionBtn'))
            .nativeElement as HTMLButtonElement;
        expect(pageCancelButton.disabled).toBeFalsy();
        pageCancelButton.click();
        fixture.detectChanges();

        expect(cancelSecondVerificationSpy).toHaveBeenCalled();
        expect(fuseConfirmationOpenDialogSpy).not.toHaveBeenCalled();
        expect(toastrSpy).toHaveBeenCalled();
        expect(routeSpy).not.toHaveBeenCalled();
    }));
});
