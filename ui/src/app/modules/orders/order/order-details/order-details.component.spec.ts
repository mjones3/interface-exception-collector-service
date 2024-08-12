import { provideHttpClient } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatIconTestingModule } from '@angular/material/icon/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { ApolloModule } from 'apollo-angular';
import { ApolloTestingModule } from 'apollo-angular/testing';
import { ToastrModule } from 'ngx-toastr';
import { of } from 'rxjs';
import { OrderService } from '../../services/order.service';
import { OrderDetailsComponent } from './order-details.component';

describe('OrderDetailsComponent', () => {
    let component: OrderDetailsComponent;
    let fixture: ComponentFixture<OrderDetailsComponent>;
    let router: Router;
    let orderService: OrderService;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                OrderDetailsComponent,
                ApolloTestingModule,
                NoopAnimationsModule,
                MatIconTestingModule,
                ApolloModule,
                TranslateModule.forRoot(),
                ToastrModule.forRoot(),
            ],
            providers: [
                provideHttpClient(),
                {
                    provide: ActivatedRoute,
                    useValue: {
                        paramMap: of({}),
                        snapshot: {
                            params: { id: 1 },
                        },
                    },
                },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(OrderDetailsComponent);
        component = fixture.componentInstance;
        router = TestBed.inject(Router);
        orderService = TestBed.inject(OrderService);

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
        jest.spyOn(orderService, 'getOrderById');
        component.fetchOrderDetails();
        expect(orderService.getOrderById).toHaveBeenCalledWith(orderId, true);
    });
});
