import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { createTestContext } from '@testing';
import { MenuComponent } from './menu.component';

describe('MenuComponent', () => {
    let component: MenuComponent;
    let fixture: ComponentFixture<MenuComponent>;

    beforeEach(waitForAsync(() => {
        TestBed.configureTestingModule({
            imports: [MenuComponent],
        }).compileComponents();
    }));

    beforeEach(() => {
        const testContext = createTestContext<MenuComponent>(MenuComponent);
        fixture = testContext.fixture;
        component = testContext.component;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
