import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FuseCardComponent } from '@fuse/components/card';
import { SearchOrdersComponent } from './search-orders.component';

describe('SearchOrdersComponent', () => {
  let component: SearchOrdersComponent;
  let fixture: ComponentFixture<SearchOrdersComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SearchOrdersComponent, FuseCardComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(SearchOrdersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
