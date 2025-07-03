import { NgClass, NgStyle } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { FuseCardComponent } from '@fuse/components/card/public-api';

@Component({
    selector: 'biopro-unit-number-card',
    standalone: true,
    imports: [
        FuseCardComponent,
        NgClass,
        NgStyle,
        MatIconModule,
        MatButtonModule,
    ],
    templateUrl: './unit-number-card.component.html',
    styleUrl: './unit-number-card.component.scss',
})
export class UnitNumberCardComponent {
    @Input() id: string | number;
    @Input() unitNumber = '';
    @Input() productName = '';
    @Input() status = '';
    @Input() statusClasses = '';
    @Input() statuses = [];
    @Input() iconName = '';
    @Input() active = false;
    @Input() visualInspection = '';
    @Input() showVisualInspection = false;
    @Input() disableActive = true;
    @Input() ineligibleStatus = '';

    @Output() clickEvent = new EventEmitter<string | number>();

    handleClick(id: number | string) {
        if (this.disableActive) {
            this.active = !this.active;
        }
        this.clickEvent.emit(id);
    }
}
