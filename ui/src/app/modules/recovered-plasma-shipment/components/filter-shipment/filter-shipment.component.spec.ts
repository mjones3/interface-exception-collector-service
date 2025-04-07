import { provideHttpClient } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { provideMockStore } from '@ngrx/store/testing';
import { FilterShipmentComponent } from './filter-shipment.component';

describe('FilterShipmentComponent', () => {
    let component: FilterShipmentComponent;
    let fixture: ComponentFixture<FilterShipmentComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [FilterShipmentComponent, NoopAnimationsModule],
            providers: [provideHttpClient(), provideMockStore({})],
        }).compileComponents();

        fixture = TestBed.createComponent(FilterShipmentComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
