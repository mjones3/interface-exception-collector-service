import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ViewShippingLabelComponent } from './view-shipping-label.component';

describe('ViewShippingLabelComponent', () => {
  let component: ViewShippingLabelComponent;
  let fixture: ComponentFixture<ViewShippingLabelComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ViewShippingLabelComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ViewShippingLabelComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
