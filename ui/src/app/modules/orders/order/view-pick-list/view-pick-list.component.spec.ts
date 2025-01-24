import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatIconTestingModule } from '@angular/material/icon/testing';
import { ViewPickListComponent } from './view-pick-list.component';
import { By } from '@angular/platform-browser';
import {
    PickListDTO,
    PickListItemDTO,
    PickListItemShortDateDTO,
} from '../../graphql/mutation-definitions/generate-pick-list.graphql';

describe('ViewPickListComponent', () => {
    let component: ViewPickListComponent;
    let fixture: ComponentFixture<ViewPickListComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [ViewPickListComponent, MatIconTestingModule],
            providers: [
                { provide: MatDialogRef, useValue: {} },
                { provide: MAT_DIALOG_DATA, useValue: {} },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(ViewPickListComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should show "No storage location available" message when storageLocation is null', () => {
        const element = fixture.debugElement;
        component.pickListModel.set({
            customer: {
                code: 'Customer Code',
                name: 'Customer Name',
            },
            orderComments: 'Order Comments',
            orderNumber: 1,
            pickListItems: [
                {
                    shortDateList: [
                        {
                            unitNumber: 'Unit Number',
                            productCode: 'Product Code',
                            aboRh: 'Abo Rh',
                        } as PickListItemShortDateDTO,
                    ],
                } as PickListItemDTO,
            ],
        } as PickListDTO);
        fixture.detectChanges();

        const storageLocationElement = element.query(
            By.css('#shortDateDetailsTable > tbody > tr > td.storage-location')
        );
        const storageLocationTd =
            storageLocationElement.nativeElement as HTMLTableCellElement;
        expect(storageLocationTd.textContent.trim()).toBe(
            ViewPickListComponent.NO_STORAGE_LOCATION_AVAILABLE_MESSAGE
        );
    });

});
