import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { SearchOrdersComponent } from './search-orders.component';

describe('SearchOrdersComponent', () => {
  let component: SearchOrdersComponent;
  let fixture: ComponentFixture<SearchOrdersComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [SearchOrdersComponent]
    });
    fixture = TestBed.createComponent(SearchOrdersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
