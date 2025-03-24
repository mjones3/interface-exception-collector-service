import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { SearchShipmentComponent } from './search-shipment.component';

describe('SearchShipmentComponent', () => {
    let component: SearchShipmentComponent;
    let fixture: ComponentFixture<SearchShipmentComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [SearchShipmentComponent, NoopAnimationsModule],
        }).compileComponents();

        fixture = TestBed.createComponent(SearchShipmentComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
