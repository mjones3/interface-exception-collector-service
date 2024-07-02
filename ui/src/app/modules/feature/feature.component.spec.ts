import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { FeatureComponent } from './feature.component';

describe('FeatureComponent', () => {
    let component: FeatureComponent;
    let fixture: ComponentFixture<FeatureComponent>;

    beforeEach(waitForAsync(() => {
        TestBed.configureTestingModule({
            imports: [FeatureComponent],
            providers: [],
        }).compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(FeatureComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
