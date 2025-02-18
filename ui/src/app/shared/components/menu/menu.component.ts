import { Component, Input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { TableMenu } from 'app/shared/models';

@Component({
    selector: 'biopro-menu',
    standalone: true,
    imports: [MatButtonModule, MatMenuModule, MatIcon],
    templateUrl: './menu.component.html',
})
export class MenuComponent {
    @Input({ required: true }) menus: TableMenu[];
}
