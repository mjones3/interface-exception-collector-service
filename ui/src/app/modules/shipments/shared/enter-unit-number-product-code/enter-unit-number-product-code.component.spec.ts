import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import {
    TranslateFakeLoader,
    TranslateLoader,
    TranslateModule,
} from '@ngx-translate/core';
import { ApolloTestingModule } from 'apollo-angular/testing';
import { ToastrModule, ToastrService } from 'ngx-toastr';
import { of } from 'rxjs';
import { ShipmentService } from '../../services/shipment.service';
import { EnterUnitNumberProductCodeComponent } from './enter-unit-number-product-code.component';

describe('EnterUnitNumberProductCodeComponent', () => {
    let component: EnterUnitNumberProductCodeComponent;
    let fixture: ComponentFixture<EnterUnitNumberProductCodeComponent>;
    let shipmentService: ShipmentService;
    let toaster: ToastrService;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                EnterUnitNumberProductCodeComponent,
                BrowserAnimationsModule,
                ApolloTestingModule,
                ToastrModule.forRoot(),
                TranslateModule.forRoot({
                    loader: {
                        provide: TranslateLoader,
                        useClass: TranslateFakeLoader,
                    },
                }),
            ],
            providers: [provideHttpClientTesting()],
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(EnterUnitNumberProductCodeComponent);
        component = fixture.componentInstance;
        shipmentService = TestBed.inject(ShipmentService);
        toaster = TestBed.inject(ToastrService);
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should enable visual inspection when the inputs for unit number and product code are valid', () => {
        component.showVisualInspection = true;
        component.productGroup.controls.unitNumber.setValue('W036898786800');
        component.productGroup.controls.productCode.setValue('E7644V00');
        jest.spyOn(component, 'checkDigitValid', 'get').mockReturnValue(true);
        component.unitNumberComponent.controlCheckDigit.setValue('E');
        component.enableVisualInspection();
        expect(
            component.productGroup.controls.visualInspection.enabled
        ).toBeTruthy();
    });

    it('should disable visual inspection when the inputs for unit number and product code are Invalid', () => {
        component.showVisualInspection = true;
        component.productGroup.controls.unitNumber.setValue('W036898786800');
        component.productGroup.controls.productCode.setValue('E7644');
        component.enableVisualInspection();
        expect(
            component.productGroup.controls.visualInspection.disabled
        ).toBeTruthy();
    });

    it('should verify the unit number and check degit if check digit is valid', () => {
        jest.spyOn(shipmentService, 'validateCheckDigit').mockReturnValue(
            of({
                data: {
                    verifyCheckDigit: {
                        ruleCode: '200 OK',
                        _links: null,
                        results: {
                            data: [
                                {
                                    unitNumber: 'W036824620959',
                                    verifiedCheckDigit: 'I',
                                },
                            ],
                        },
                        notifications: null,
                    },
                },
            })
        );

        component.verifyUnit({
            unitNumber: 'W036824620959',
            checkDigit: 'I',
            scanner: false,
            checkDigitChange: true,
        });
        expect(component.verifyUnit).toHaveLength(1);
    });

    it('should return Invalid Message when the check digit is Invalid ', () => {
        jest.spyOn(shipmentService, 'validateCheckDigit').mockReturnValue(
            of({
                data: {
                    verifyCheckDigit: {
                        ruleCode: '400 BAD_REQUEST',
                        _links: null,
                        results: null,
                        notifications: [
                            {
                                name: 'INVALID_CHECK_DIGIT',
                                statusCode: 400,
                                notificationType: 'WARN',
                                code: 400,
                                action: null,
                                reason: null,
                                message: 'Check Digit is Invalid',
                            },
                        ],
                    },
                },
            })
        );

        component.verifyUnit({
            unitNumber: 'W036824620959',
            checkDigit: 'k',
            scanner: false,
            checkDigitChange: true,
        });
        expect(
            component.unitNumberComponent.checkDigitInvalidMessage
        ).toBeTruthy();
    });
});
