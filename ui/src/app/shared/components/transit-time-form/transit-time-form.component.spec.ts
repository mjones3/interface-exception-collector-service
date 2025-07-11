import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TransitTimeFormComponent } from './transit-time-form.component';
import { ReactiveFormsModule } from '@angular/forms';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';

jest.mock('keycloak-js');

describe('TransitTimeFormComponent', () => {
  let component: TransitTimeFormComponent;
  let fixture: ComponentFixture<TransitTimeFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        TransitTimeFormComponent,
        ReactiveFormsModule,
        NoopAnimationsModule,
        MatDatepickerModule,
        MatNativeDateModule
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(TransitTimeFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});