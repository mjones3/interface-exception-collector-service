import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { TemperatureFormComponent } from './temperature-form.component';

jest.mock('keycloak-js');

describe('TemperatureFormComponent', () => {
  let component: TemperatureFormComponent;
  let fixture: ComponentFixture<TemperatureFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        TemperatureFormComponent,
        ReactiveFormsModule,
        NoopAnimationsModule,
        MatDatepickerModule,
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(TemperatureFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});