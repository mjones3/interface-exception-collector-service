import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { ActionButtonComponent } from 'app/shared/components/action-button/action-button.component';
import { AcknowledgeDetailDTO } from 'app/shared/models';

@Component({
    selector: 'app-acknowledge-confirmation',
    standalone: true,
    imports: [MatDialogModule, ActionButtonComponent],
    templateUrl: './acknowledge-confirmation.component.html',
})
export class AcknowledgeConfirmationComponent {
    data: AcknowledgeDetailDTO;

    constructor(@Inject(MAT_DIALOG_DATA) data: AcknowledgeDetailDTO) {
        this.data = data;
    }
}
