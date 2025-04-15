import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ShippingCartonInformationCardComponent } from './shipping-carton-information-card.component';

describe('ShippingCartonInformationCardComponent', () => {
  let component: ShippingCartonInformationCardComponent;
  let fixture: ComponentFixture<ShippingCartonInformationCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ShippingCartonInformationCardComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ShippingCartonInformationCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
