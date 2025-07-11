import { DatePipe } from '@angular/common';
import { provideHttpClient } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatIconTestingModule } from '@angular/material/icon/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { provideMockStore } from '@ngrx/store/testing';
import { TranslateModule } from '@ngx-translate/core';
import { ApolloTestingModule } from 'apollo-angular/testing';
import { ProductIconsService } from 'app/shared/services/product-icon.service';
import { ToastrModule } from 'ngx-toastr';
import { of } from 'rxjs';
import { OrderService } from '../../services/order.service';
import { OrderDetailsComponent } from './order-details.component';
import { AuthState } from 'app/core/state/auth/auth.reducer';
import { TagComponent } from '../../../../shared/components/tag/tag.component';
import { OrderDetailsDTO } from '../../models/order-details.dto';
import { By } from '@angular/platform-browser';

describe('OrderDetailsComponent', () => {
    let component: OrderDetailsComponent;
    let fixture: ComponentFixture<OrderDetailsComponent>;
    let router: Router;
    let orderService: OrderService;
    let productIconService: ProductIconsService;
    let datePipe: DatePipe;

    const initialState: AuthState = {
        id: 'mock-user-id',
        loaded: true,
    };

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                OrderDetailsComponent,
                ApolloTestingModule,
                NoopAnimationsModule,
                MatIconTestingModule,
                TranslateModule.forRoot(),
                ToastrModule.forRoot(),
                TagComponent
            ],
            providers: [
                provideHttpClient(),
                provideMockStore({ initialState }),
                DatePipe,
                {
                    provide: ActivatedRoute,
                    useValue: {
                        paramMap: of({}),
                        snapshot: {
                            params: { id: 1 },
                        },
                    },
                },
                OrderService,
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(OrderDetailsComponent);
        component = fixture.componentInstance;
        router = TestBed.inject(Router);
        productIconService = TestBed.inject(ProductIconsService);
        orderService = TestBed.inject(OrderService);
        datePipe = TestBed.inject(DatePipe);
        jest.spyOn(orderService, 'getOrderById').mockReturnValue(of());
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should navigate back to search order', () => {
        jest.spyOn(router, 'navigateByUrl');
        component.backToSearch();
        expect(router.navigateByUrl).toHaveBeenCalledWith('/orders/search');
    });

    it('should fetch order details', () => {
        const orderId = 1;
        component.fetchOrderDetails();
        expect(orderService.getOrderById).toHaveBeenCalledWith(orderId);
    });

    it('should get icon based on product family', () => {
        let productFamily: string;
        jest.spyOn(
            productIconService,
            'getIconByProductFamily'
        ).mockReturnValue('WHOLE_BLOOD_LEUKOREDUCED');
        component.getIcon(productFamily);
        fixture.detectChanges();
        expect(component.getIcon(productFamily)).toBe(
            'WHOLE_BLOOD_LEUKOREDUCED'
        );
    });

    describe('inventory availability conditions', () => {
        it('should show unavailable tag when shipmentType is INTERNAL_TRANSFER', () => {

            const testProduct = {
                quantityAvailable: null
            };
            component.orderDetails = {
                shipmentType: 'INTERNAL_TRANSFER',
                orderItems: [testProduct]
            } as OrderDetailsDTO;
            component.notifications = [];
            
            fixture.detectChanges();
            const tagElement = fixture.debugElement.query(By.directive(TagComponent));
            expect(tagElement).toBeTruthy();
        });

        it('should show quantity available when neither condition is true', () => {
            const testProduct = {
                quantityAvailable: 10
            };
            component.orderDetails = {
                shipmentType: 'CUSTOMER',
                orderItems: [testProduct]
            } as OrderDetailsDTO;
            component.notifications = [];
            
            fixture.detectChanges();
            const tagElement = fixture.debugElement.query(By.directive(TagComponent));
            expect(tagElement).toBeFalsy();
        });
    });
});
