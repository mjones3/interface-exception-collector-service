import { ComponentFixture, TestBed } from '@angular/core/testing';

import { provideHttpClientTesting } from '@angular/common/http/testing';
import { MatIconTestingModule } from '@angular/material/icon/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { provideMockStore } from '@ngrx/store/testing';
import { ApolloModule } from 'apollo-angular';
import { ApolloTestingModule } from 'apollo-angular/testing';
import { ToastrModule } from 'ngx-toastr';
import { of } from 'rxjs';
import { ShipmentService } from '../services/shipment.service';
import { VerifyProductsNotificationsComponent } from './verify-products-notifications.component';

const SHIPMENT_ID = 1;
describe('VerifyProductsNotificationsComponent', () => {
    let component: VerifyProductsNotificationsComponent;
    let fixture: ComponentFixture<VerifyProductsNotificationsComponent>;
    let shipmentService: ShipmentService;
    let router: Router;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                VerifyProductsNotificationsComponent,
                ApolloTestingModule,
                NoopAnimationsModule,
                MatIconTestingModule,
                ApolloModule,
                ToastrModule.forRoot(),
            ],
            providers: [
                provideHttpClientTesting(),
                provideMockStore({}),
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
                ShipmentService,
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(VerifyProductsNotificationsComponent);
        component = fixture.componentInstance;
        shipmentService = TestBed.inject(ShipmentService);
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
});
