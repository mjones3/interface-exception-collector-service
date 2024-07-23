import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { ApolloModule } from 'apollo-angular';
import { ToastrModule, ToastrService } from 'ngx-toastr';
import { SearchOrdersComponent } from './search-orders.component';

describe('SearchOrdersComponent', () => {
  let component: SearchOrdersComponent;
  let fixture: ComponentFixture<SearchOrdersComponent>;
  let mockToastr: ToastrService

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        SearchOrdersComponent,
        ApolloModule,
        ToastrModule.forRoot()
      ],
      providers: [
        {
          provide: ToastrService,
          useClass: mockToastr
        }
      ]
    });
    fixture = TestBed.createComponent(SearchOrdersComponent);
    component = fixture.componentInstance;
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
