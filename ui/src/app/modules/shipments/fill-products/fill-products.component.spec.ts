import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FillProductsComponent } from './fill-products.component';

describe('FillProductsComponent', () => {
  let component: FillProductsComponent;
  let fixture: ComponentFixture<FillProductsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        FillProductsComponent
      ]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(FillProductsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
