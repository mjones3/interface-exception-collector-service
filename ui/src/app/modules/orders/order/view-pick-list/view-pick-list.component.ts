import { AsyncPipe } from '@angular/common';
import { Component } from '@angular/core';
import { MatIconButton } from '@angular/material/button';
import { MatDialogClose } from '@angular/material/dialog';
import { MatIcon } from '@angular/material/icon';
import { Observable } from 'rxjs';
import { DEFAULT_PAGE_SIZE } from '../../../../core/models/browser-printing.model';
import { BrowserPrintingService } from '../../../../core/services/browser-printing/browser-printing.service';
import { ProductFamilyMap } from '../../../../shared/models/product-family.model';
import { PickListDTO } from '../../graphql/mutation-definitions/generate-pick-list.graphql';

@Component({
    selector: 'app-view-pick-list',
    standalone: true,
    imports: [MatIcon, MatIconButton, MatDialogClose, AsyncPipe],
    templateUrl: './view-pick-list.component.html',
})
export class ViewPickListComponent {
    protected readonly ProductFamilyMap = ProductFamilyMap;

    model$: Observable<PickListDTO>;

    constructor(private browserPrintingService: BrowserPrintingService) {}

    hasAnyShortDate(model: PickListDTO): boolean {
        return !!model?.pickListItems?.some((i) => i.shortDateList?.length);
    }

    print(): void {
        this.browserPrintingService.print('viewPickListReport', {
            pageSize: DEFAULT_PAGE_SIZE,
        });
    }
}
