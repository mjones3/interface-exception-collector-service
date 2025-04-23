import { CommonModule, NgClass } from '@angular/common';
import { Component, Input, ViewEncapsulation } from '@angular/core';
import { MatProgressBarModule } from '@angular/material/progress-bar';

@Component({
    selector: 'biopro-progress-bar',
    standalone: true,
    imports: [MatProgressBarModule, NgClass, CommonModule],
    templateUrl: './progress-bar.component.html',
    styleUrl: './progress-bar.component.scss',
    encapsulation: ViewEncapsulation.None,
})
export class ProgressBarComponent {
    @Input() value = 0;
    @Input() progressBarId: string;
}
