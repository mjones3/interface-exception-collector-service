import { model } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import {
    LinkLabel,
    LinkRoute,
    RecoveredPlasmaShipmentDetailsNavbarComponent,
} from './recovered-plasma-shipment-details-navbar.component';

describe('RecoveredPlasmaShipmentDetailsNavbarComponent', () => {
    let component: RecoveredPlasmaShipmentDetailsNavbarComponent;
    let fixture: ComponentFixture<RecoveredPlasmaShipmentDetailsNavbarComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                RecoveredPlasmaShipmentDetailsNavbarComponent,
                NoopAnimationsModule,
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(
            RecoveredPlasmaShipmentDetailsNavbarComponent
        );
        component = fixture.componentInstance;
        TestBed.runInInjectionContext(() => {
            component.links = model<Record<LinkLabel, LinkRoute>>({
                Verified: '/',
            });
        });
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
