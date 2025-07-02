import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StartIrradiationComponent } from './start-irradiation.component';

describe('StartIrradiationComponent', () => {
  let component: StartIrradiationComponent;
  let fixture: ComponentFixture<StartIrradiationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StartIrradiationComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(StartIrradiationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
