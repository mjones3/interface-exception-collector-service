import { Component, Inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import {
    MAT_DIALOG_DATA,
    MatDialogActions,
    MatDialogClose,
    MatDialogContent,
    MatDialogRef,
    MatDialogTitle,
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { Notification } from '../models/notification.dto';

@Component({
    selector: 'app-skip-inventory-unavailable-dialog',
    standalone: true,
    imports: [
        MatIconModule,
        MatFormFieldModule,
        MatInputModule,
        FormsModule,
        MatButtonModule,
        MatDialogTitle,
        MatDialogContent,
        MatDialogActions,
        MatDialogClose,
    ],
    templateUrl: './skip-inventory-unavailable-dialog.component.html',
})
export class SkipInventoryUnavailableDialogComponent {
    constructor(
        public dialogRef: MatDialogRef<SkipInventoryUnavailableDialogComponent>,
        @Inject(MAT_DIALOG_DATA) public notification: Notification
    ) {}
}
