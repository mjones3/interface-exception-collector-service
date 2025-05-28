import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ShipmentCommentsComponent } from './shipment-comments.component';

describe('ShipmentCommentsComponent', () => {
  let component: ShipmentCommentsComponent;
  let fixture: ComponentFixture<ShipmentCommentsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ShipmentCommentsComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ShipmentCommentsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
