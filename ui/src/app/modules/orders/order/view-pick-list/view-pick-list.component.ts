import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { MatIconButton } from '@angular/material/button';
import {
    MAT_DIALOG_DATA,
    MatDialogClose,
    MatDialogRef,
} from '@angular/material/dialog';
import { MatIcon } from '@angular/material/icon';
import { BrowserPrintingService } from '../../../../core/services/browser-printing/browser-printing.service';
import { ProductFamilyMap } from '../../../../shared/models/product-family.model';
import { PickListDTO } from '../../graphql/mutation-definitions/generate-pick-list.graphql';

export interface ViewPickListData {
    pickListDTO: PickListDTO;
    skipInventoryUnavailable: boolean;
}

@Component({
    selector: 'app-view-pick-list',
    standalone: true,
    imports: [MatIcon, MatIconButton, MatDialogClose],
    templateUrl: './view-pick-list.component.html',
})
export class ViewPickListComponent implements OnInit {

    static readonly NO_STORAGE_LOCATION_AVAILABLE_MESSAGE = 'No storage location available';

    readonly ProductFamilyMap = ProductFamilyMap;

    dialogRef = inject(MatDialogRef<ViewPickListComponent>);
    dialogData = inject<ViewPickListData>(MAT_DIALOG_DATA);
    browserPrintingService = inject(BrowserPrintingService);

    pickListModel = signal<PickListDTO>(null);
    skipInventoryUnavailable = signal<boolean>(false);
    hasAnyShortDateItem = computed(
        () =>
            !!this.pickListModel()?.pickListItems?.some(
                (i) => i.shortDateList?.length
            )
    );
    noStorageLocationAvailableMessage = computed(() => ViewPickListComponent.NO_STORAGE_LOCATION_AVAILABLE_MESSAGE);

    ngOnInit(): void {
        this.pickListModel.set(this.dialogData.pickListDTO);
        this.skipInventoryUnavailable.set(
            this.dialogData.skipInventoryUnavailable
        );
    }

    print(): void {
        this.browserPrintingService.print('viewPickListReport', {
            pageSize: 'A4 landscape',
        });
    }
}
