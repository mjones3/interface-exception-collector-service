import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { CreateShipmentComponent } from '../create-shipment/create-shipment.component';
import { SearchShipmentComponent } from './search-shipment.component';
import { ApolloTestingModule } from 'apollo-angular/testing';

describe('SearchShipmentComponent', () => {
    let component: SearchShipmentComponent;
    let fixture: ComponentFixture<SearchShipmentComponent>;
    let matDialog: MatDialog;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                SearchShipmentComponent,
                NoopAnimationsModule,
                CreateShipmentComponent,
                ApolloTestingModule
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(SearchShipmentComponent);
        component = fixture.componentInstance;
        matDialog = TestBed.inject(MatDialog);
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should open createShipment dialog', () => {
        jest.spyOn(matDialog, 'open').mockReturnValue(
            {} as MatDialogRef<CreateShipmentComponent>
        );
        component.openCreateShipment();
        expect(component).toBeTruthy();
        expect(matDialog.open).toHaveBeenCalled();
    });
});
