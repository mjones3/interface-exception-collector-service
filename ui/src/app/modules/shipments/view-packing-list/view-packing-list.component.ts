import { AsyncPipe, DatePipe } from '@angular/common';
import { Component } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { Observable, of } from 'rxjs';
import { ProductFamilyMap } from '../../../shared/models/product-family.model';
import { PackingListLabelDTO } from '../models/packing-list.model';

@Component({
    selector: 'app-view-packing-list',
    standalone: true,
    imports: [AsyncPipe, DatePipe],
    templateUrl: './view-packing-list.component.html',
})
export class ViewPackingListComponent {
    model$: Observable<Partial<PackingListLabelDTO>> = of();

    constructor(private domSanitizer: DomSanitizer) {}

    getPackedItemsQuantity(packingListLabelDTO: Partial<PackingListLabelDTO>) {
        return packingListLabelDTO?.packedItems?.length ?? 0;
    }

    getBase64DataImage(payload: string): SafeResourceUrl {
        return this.domSanitizer.bypassSecurityTrustResourceUrl(
            `data:image/*;base64,${payload}`
        );
    }

    get navigatorLanguage() {
        return navigator.languages?.[0] ?? navigator.language;
    }

    get localTimezone() {
        const dateParts = new Date()
            .toLocaleTimeString(this.navigatorLanguage, {
                timeZoneName: 'short',
            })
            .split(' ');

        return dateParts?.length > 0 ? dateParts[dateParts.length - 1] : '';
    }

    protected readonly ProductFamilyMap = ProductFamilyMap;
}
