import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { provideMockStore } from '@ngrx/store/testing';
import { TranslateModule } from '@ngx-translate/core';
import { ApolloModule } from 'apollo-angular';
import { ApolloTestingModule } from 'apollo-angular/testing';
import { ToastrModule } from 'ngx-toastr';
import { ShipmentInfoItemDto } from '../models/shipment-info.dto';
import { ShipmentService } from '../services/shipment.service';
import { ShipmentDetailsComponent } from './shipment-details.component';

const SHIPMENT_ID = 1;
describe('ShipmentDetailsComponent', () => {
    let component: ShipmentDetailsComponent;
    let fixture: ComponentFixture<ShipmentDetailsComponent>;
    let shipmentService: ShipmentService;
    let router: Router;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                ShipmentDetailsComponent,
                ApolloTestingModule,
                NoopAnimationsModule,
                ApolloModule,
                TranslateModule.forRoot(),
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
                            data: {
                                shipmentDetailsConfigData: [],
                            },
                        },
                    },
                },
            ],
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(ShipmentDetailsComponent);
        component = fixture.componentInstance;
        shipmentService = TestBed.inject(ShipmentService);
        router = TestBed.inject(Router);
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should fetch shipment details', () => {
        jest.spyOn(shipmentService, 'getShipmentById');
        component.fetchShipmentDetails();
        expect(shipmentService.getShipmentById).toHaveBeenCalledWith(
            SHIPMENT_ID,
            true
        );
    });

    it('should navigate back to search order', () => {
        jest.spyOn(router, 'navigateByUrl');
        component.backToSearch();
        expect(router.navigateByUrl).toHaveBeenCalledWith('/orders/search');
    });

    it('should navigate to fill product when click on Fill Product button', () => {
        const shipment = {
            id: '1',
        };
        jest.spyOn(router, 'navigateByUrl');
        component.fillProducts(shipment as ShipmentInfoItemDto);
        expect(router.navigateByUrl).toHaveBeenCalledWith(
            'shipment/1/fill-products/1'
        );
    });

    it('should complete on click complete shipment button', () => {
        component.loggedUserId = 'user-id-12';
        jest.spyOn(shipmentService, 'completeShipment');
        component.completeShipment();
        expect(shipmentService.completeShipment).toHaveBeenCalledWith({
            shipmentId: 1,
            employeeId: 'user-id-12',
        });
    });
});
