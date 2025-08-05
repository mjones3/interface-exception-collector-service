import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { AcknowledgeDetailDTO } from 'app/shared/models';
import { ActionButtonComponent } from '../buttons/action-button.component';

@Component({
    selector: 'app-acknowledge-confirmation',
    standalone: true,
    imports: [MatDialogModule, ActionButtonComponent],
    templateUrl: './acknowledge-confirmation.component.html',
    styles: [
        `
            .confirmation-width {
                min-width: 30rem !important;
                max-width: 40rem !important;
            }
        `,
    ],
})
export class AcknowledgeConfirmationComponent {
    data: AcknowledgeDetailDTO;

    constructor(@Inject(MAT_DIALOG_DATA) data: AcknowledgeDetailDTO) {
        this.data = data;
    }
}
