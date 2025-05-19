import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { MatIconTestingModule } from '@angular/material/icon/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { provideMockStore } from '@ngrx/store/testing';
import { ApolloTestingModule } from 'apollo-angular/testing';
import { ToastrModule } from 'ngx-toastr';
import { of } from 'rxjs';
import { ShipmentService } from '../services/shipment.service';
import { VerifyProductsNotificationsComponent } from './verify-products-notifications.component';
import { AuthState } from 'app/core/state/auth/auth.reducer';

const SHIPMENT_ID = 1;
describe('VerifyProductsNotificationsComponent', () => {
    let component: VerifyProductsNotificationsComponent;
    let fixture: ComponentFixture<VerifyProductsNotificationsComponent>;
    let shipmentService: ShipmentService;
    let router: Router;

    const initialState: AuthState = {
        id: 'mock-user-id',
        loaded: true,
    };

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                VerifyProductsNotificationsComponent,
                ApolloTestingModule,
                NoopAnimationsModule,
                MatIconTestingModule,
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
