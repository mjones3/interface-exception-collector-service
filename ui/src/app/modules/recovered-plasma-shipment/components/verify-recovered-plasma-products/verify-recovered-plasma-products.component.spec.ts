import { ComponentFixture, TestBed } from '@angular/core/testing';
import { VerifyRecoveredPlasmaProductsComponent } from './verify-recovered-plasma-products.component';
import { provideMockStore } from '@ngrx/store/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { ToastrModule } from 'ngx-toastr';
import { ApolloTestingModule } from 'apollo-angular/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

describe('VerifyRecoveredPlasmaProductsComponent', () => {
  let component: VerifyRecoveredPlasmaProductsComponent;
  let fixture: ComponentFixture<VerifyRecoveredPlasmaProductsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        VerifyRecoveredPlasmaProductsComponent,
        ApolloTestingModule, 
        NoopAnimationsModule,
        ToastrModule.forRoot(),
      ],
      providers:[provideMockStore(),
        {
            provide: ActivatedRoute,
            useValue: {
                paramMap: of({})
            },
        },]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(VerifyRecoveredPlasmaProductsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
