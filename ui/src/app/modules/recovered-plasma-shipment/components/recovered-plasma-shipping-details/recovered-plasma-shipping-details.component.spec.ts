import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { RecoveredPlasmaShippingDetailsComponent } from './recovered-plasma-shipping-details.component';

describe('RecoveredPlasmaShippingDetailsComponent', () => {
    let component: RecoveredPlasmaShippingDetailsComponent;
    let fixture: ComponentFixture<RecoveredPlasmaShippingDetailsComponent>;
    let router: Router;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [RecoveredPlasmaShippingDetailsComponent],
            providers: [{ provide: ActivatedRoute, useValue: {} }],
        }).compileComponents();

        fixture = TestBed.createComponent(
            RecoveredPlasmaShippingDetailsComponent
        );
        component = fixture.componentInstance;
        router = TestBed.inject(Router);
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
