import { CommonModule, DatePipe } from '@angular/common';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatIconTestingModule } from '@angular/material/icon/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ProcessHeaderService } from '@shared';
import { ApolloTestingModule } from 'apollo-angular/testing';
import { ScanUnitNumberProductCodeComponent } from 'app/shared/components/scan-unit-number-product-code/scan-unit-number-product-code.component';
import { cartonDetailsComponent } from './carton-details.component';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { provideMockStore } from '@ngrx/store/testing';
import { ToastrModule } from 'ngx-toastr';

describe('cartonDetailsComponent', () => {
    let component: cartonDetailsComponent;
    let fixture: ComponentFixture<cartonDetailsComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                cartonDetailsComponent,
                MatIconTestingModule,
                NoopAnimationsModule,
                CommonModule,
                ApolloTestingModule,
                ScanUnitNumberProductCodeComponent,
                ToastrModule.forRoot(),
            ],
            providers: [
                DatePipe,
                provideMockStore(),
                {
                    provide: ActivatedRoute,
                    useValue: {
                        paramMap: of({})
                    },
                },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(cartonDetailsComponent);
        component = fixture.componentInstance;
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
