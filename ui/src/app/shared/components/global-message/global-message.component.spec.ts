import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatIconTestingModule } from '@angular/material/icon/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { createTestContext } from '@testing';
import { GlobalMessageComponent } from './global-message.component';

describe('GlobalMessageComponent', () => {
    let component: GlobalMessageComponent;
    let fixture: ComponentFixture<GlobalMessageComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                GlobalMessageComponent,
                MatIconTestingModule,
                NoopAnimationsModule,
            ],
        }).compileComponents();
    });

    beforeEach(() => {
        const testContext = createTestContext<GlobalMessageComponent>(
            GlobalMessageComponent
        );
        fixture = testContext.fixture;
        component = testContext.component;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should set the default title when not title was provided', () => {
        component.messageType = 'info';
        component.ngOnInit();
        expect(component.messageTitle).toEqual('System');
    });

    it('should emit the dismissed', () => {
        jest.spyOn(component.dismissed, 'emit');
        component.dismiss();
        expect(component.dismissed.emit).toHaveBeenCalled();
    });
});
