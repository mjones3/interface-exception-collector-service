import { ComponentFixture, TestBed } from '@angular/core/testing';
import { VerifyCloseCartonComponent } from './add-verify-close-carton.component';
import { ActivatedRoute, Router } from '@angular/router';
import { MatStepperModule } from '@angular/material/stepper';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { provideMockStore } from '@ngrx/store/testing';
import { of } from 'rxjs';
import { ToastrModule } from 'ngx-toastr';
import { ApolloTestingModule } from 'apollo-angular/testing';
import { DatePipe } from '@angular/common';
import { RecoveredPlasmaService } from '../../services/recovered-plasma.service';
import { ToastrImplService } from '@shared';

describe('VerifyCloseCartonComponent', () => {
  let component: VerifyCloseCartonComponent;
  let fixture: ComponentFixture<VerifyCloseCartonComponent>;
  let router: Router;
  let service: RecoveredPlasmaService;
  let toaster: ToastrImplService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        VerifyCloseCartonComponent,
        MatStepperModule,
        NoopAnimationsModule,
        ApolloTestingModule,
        ToastrModule.forRoot(),
      ],
      providers: [
        provideMockStore(),
        DatePipe,
        {
            provide: ActivatedRoute,
            useValue: {
                paramMap: of({})
            },
        },
    ],
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(VerifyCloseCartonComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    service = TestBed.inject(RecoveredPlasmaService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

});
