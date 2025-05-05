import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ViewShippingCartonPackingSlipComponent } from './view-shipping-carton-packing-slip.component';

describe('ViewShippingCartonPackingSlipComponent', () => {
  let component: ViewShippingCartonPackingSlipComponent;
  let fixture: ComponentFixture<ViewShippingCartonPackingSlipComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ViewShippingCartonPackingSlipComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ViewShippingCartonPackingSlipComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
