import {
    Component,
    ViewEncapsulation,
    booleanAttribute,
    input,
    output,
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
    selector: 'biopro-basic-button',
    standalone: true,
    imports: [MatButtonModule, MatIconModule],
    template: `
        <button
            [id]="id()"
            mat-button
            type="button"
            [color]="color()"
            [disabled]="disabled()"
            [class]="class()"
            (click)="onClick()"
        >
            @if (icon()) {
                <mat-icon [svgIcon]="icon()"></mat-icon>
            }
            {{ label() }}
        </button>
    `,
    styleUrl: './button.scss',
    encapsulation: ViewEncapsulation.None,
})
export class BasicButtonComponent {
    id = input.required<string>();
    label = input.required<string>();
    class = input<'string'>();
    color = input.required<'primary' | 'secondary'>();
    icon = input<'string'>();
    disabled = input(false, { transform: booleanAttribute });

    buttonClicked = output();

    onClick() {
        this.buttonClicked.emit();
    }
}
