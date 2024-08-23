import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatIconTestingModule } from '@angular/material/icon/testing';
import { ViewPickListComponent } from './view-pick-list.component';

describe('ViewPickListComponent', () => {
    let component: ViewPickListComponent;
    let fixture: ComponentFixture<ViewPickListComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [ViewPickListComponent, MatIconTestingModule],
        }).compileComponents();

        fixture = TestBed.createComponent(ViewPickListComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
