import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UnitNumberCardComponent } from './unit-number-card.component';

describe('UnitNumberCardComponent', () => {
    let component: UnitNumberCardComponent;
    let fixture: ComponentFixture<UnitNumberCardComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [UnitNumberCardComponent],
        }).compileComponents();

        fixture = TestBed.createComponent(UnitNumberCardComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
