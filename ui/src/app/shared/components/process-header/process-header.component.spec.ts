import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProcessHeaderComponent } from './process-header.component';

describe.skip('ProcessHeaderComponent', () => {
    let component: ProcessHeaderComponent;
    let fixture: ComponentFixture<ProcessHeaderComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [ProcessHeaderComponent],
        }).compileComponents();

        fixture = TestBed.createComponent(ProcessHeaderComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
