import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CloseIrradiationComponent } from './close-irradiation.component';

describe('CloseIrradiationComponent', () => {
  let component: CloseIrradiationComponent;
  let fixture: ComponentFixture<CloseIrradiationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CloseIrradiationComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(CloseIrradiationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
