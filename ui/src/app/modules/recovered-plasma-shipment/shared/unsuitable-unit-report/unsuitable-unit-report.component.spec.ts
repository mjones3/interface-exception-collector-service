import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UnsuitableUnitReportComponent } from './unsuitable-unit-report.component';

describe('UnsuitableUnitReportComponent', () => {
  let component: UnsuitableUnitReportComponent;
  let fixture: ComponentFixture<UnsuitableUnitReportComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UnsuitableUnitReportComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(UnsuitableUnitReportComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
