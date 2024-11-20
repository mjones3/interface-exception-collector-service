import { CommonModule } from '@angular/common';
import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatTabsModule } from '@angular/material/tabs';
import {
    InventoryResponseDTO,
    NotificationModalDTO,
    NotificationTabDTO,
} from 'app/shared/models/rule.model';
import { ProductIconsService } from 'app/shared/services/product-icon.service';
import { ActionButtonComponent } from '../action-button/action-button.component';
import { UnitNumberCardComponent } from '../unit-number-card/unit-number-card.component';

@Component({
    selector: 'app-notification',
    standalone: true,
    imports: [
        MatTabsModule,
        UnitNumberCardComponent,
        ActionButtonComponent,
        CommonModule,
    ],
    templateUrl: './notification.component.html',
    styleUrl: './notification.component.scss',
})
export class NotificationComponent {
    inventoryProducts: NotificationTabDTO[] = [];
    constructor(
        private productIconService: ProductIconsService,
        @Inject(MAT_DIALOG_DATA) public data: NotificationModalDTO
    ) {
        this.inventoryProducts =
            this.generateTabsWithProductsGroupByErrorName(data);
    }

    generateTabsWithProductsGroupByErrorName(
        data: NotificationModalDTO
    ): NotificationTabDTO[] {
        const errorNameMappings = {
            INVENTORY_NOT_FOUND_IN_LOCATION: {
                tabName: 'Other Events',
                label: 'Inventory Not Found',
            },
            INVENTORY_NOT_EXIST: {
                tabName: 'Other Events',
                label: 'Inventory Not Found',
            },
            INVENTORY_IS_SHIPPED: {
                tabName: 'Other Events',
                label: 'Already Shipped',
            },
            INVENTORY_IS_EXPIRED: { tabName: 'Discarded', label: 'Expired' },
            INVENTORY_IS_DISCARDED: {
                tabName: 'Discarded',
                label: 'Discarded',
            },
            INVENTORY_IS_UNSUITABLE: {
                tabName: 'Discarded',
                label: 'Unsuitable',
            },
            INVENTORY_IS_QUARANTINED: {
                tabName: 'Quarantined',
                label: 'Quarantined',
            },
        };

        return data?.data?.results?.validations.reduce(
            (acc: NotificationTabDTO[], current: InventoryResponseDTO) => {
                current?.inventoryNotificationsDTO.forEach((notification) => {
                    const { tabName, label } =
                        errorNameMappings[notification.errorName] || {};
                    if (tabName) {
                        let group = acc.find((g) => g.tabName === tabName);
                        if (!group) {
                            group = { tabName, inventoryResponse: [] };
                            acc.push(group);
                        }
                        current.inventoryResponseDTO.label = label;
                        group.inventoryResponse.push(
                            current?.inventoryResponseDTO
                        );
                    }
                });
                return acc;
            },
            []
        );
    }

    getItemIcon(productFamily: string) {
        return this.productIconService.getIconByProductFamily(productFamily);
    }
}
