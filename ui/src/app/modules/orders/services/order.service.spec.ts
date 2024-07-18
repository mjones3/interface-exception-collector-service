import { TestBed } from '@angular/core/testing';
import { ApolloModule } from 'apollo-angular';
import { ApolloTestingController, ApolloTestingModule } from 'apollo-angular/testing';
import { OrderService } from './order.service';

describe('OrderService', () => {
  let service: OrderService;
  let controller: ApolloTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        ApolloTestingModule,
        ApolloModule
      ],
      providers: [
        OrderService,
      ],
    });

    service = TestBed.inject(OrderService);
    controller = TestBed.inject(ApolloTestingController);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
