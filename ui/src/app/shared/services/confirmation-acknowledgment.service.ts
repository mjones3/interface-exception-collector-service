import { Injectable } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import {
    AcknowledgeConfirmationComponent
} from '../components/acknowledge-confirmation/acknowledge-confirmation.component';
import { AcknowledgeDetailDTO } from '@shared';

@Injectable({
    providedIn: 'root',
})
export class ConfirmationAcknowledgmentService {

    constructor(private matDialog: MatDialog) {}

    openAcknowledgmentDialog(message: string, details: string[], callBackFn: () => void = () => {}): void {
        const acknowledgeDetail: AcknowledgeDetailDTO = {
            title: 'Acknowledgment Message',
            description: message,
            subtitle: 'Details',
            details: details,
        };

        this.matDialog
            .open(AcknowledgeConfirmationComponent, {
                disableClose: true,
                data: acknowledgeDetail,
            })
            .afterClosed()
            .subscribe(() => {
                callBackFn();
            });
    }

}
