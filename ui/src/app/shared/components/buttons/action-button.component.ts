import {
    booleanAttribute,
    Component,
    EventEmitter,
    Input,
    Output,
} from '@angular/core';
import { MatFabButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';

@Component({
    selector: 'biopro-action-button',
    template: `
        <button
            mat-fab
            extended
            type="button"
            [id]="btnId"
            [color]="color"
            [disabled]="disabled"
            (click)="click()"
            [class]="class"
        >
            @if (icon) {
                <mat-icon [svgIcon]="icon"></mat-icon>
            }
            {{ label }}
        </button>
    `,
    standalone: true,
    imports: [MatFabButton, MatIcon],
})
export class ActionButtonComponent {
    @Input({ required: true }) btnId: string;
    @Input({ required: true }) label: string;
    @Input() color = 'primary';
    @Input() icon: string;
    @Input({ transform: booleanAttribute }) disabled: boolean;
    @Input() class: string;

    @Output() clickEvent = new EventEmitter<void>();

    click() {
        this.clickEvent.emit();
    }
}
