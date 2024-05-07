import { Component, Input, TemplateRef } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterModule } from '@angular/router';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { MaterialModule } from '@rsa/material';
import { boolean, text } from '@storybook/addon-knobs';
import { TouchableComponentsModule } from '../touchable-components.module';
import { ConfirmationDialogComponent } from './confirmation-dialog.component';


export default {
  title: 'rsa-confirmation-dialog'
};

// Wrapper Component
@Component({
  template: `
    <div class="flex justify-center items-center h-full w-full" *ngIf="!commentsModal">
      <button mat-raised-button color="primary" (click)="openStandardModal()">Launch Standard Confirmation Dialog
      </button>
    </div>

    <div class="flex justify-center items-center h-full w-full" *ngIf="commentsModal">
      <button mat-raised-button color="primary" (click)="openCommentsModal(commentsDialog)">Launch Confirmation Dialog
        with Comments
      </button>
    </div>

    <!-- Template for Dialog With Comments -->
    <ng-template #commentsDialog>
      <rsa-confirmation-dialog [dialogTitle]="dialogTitle" [acceptBtnTittle]="acceptBtnTittle"
                               [cancelBtnTittle]="cancelBtnTittle" [commentsModal]="true" [commentsLabel]="commentsLabel">
        <div class="flex flex-col bg-white-200 py-4">
          <div class="text-gray-700 text-left pl-4"><strong>Unit Number:</strong> Wxxxxxxxxxxxx</div>
          <div class="text-gray-700 text-left pl-4"><strong>Blood Type:</strong> O Positive</div>
          <div class="text-gray-700 text-left pl-4"><strong>Collection Date:</strong> 07/24/2020</div>
          <div class="text-gray-700 text-left pl-4"><strong>Draw Time:</strong> 07/24/2020 14:15 EDT</div>
        </div>
      </rsa-confirmation-dialog>
    </ng-template>
  `
})
class ConfirmationWrapperComponent {

  @Input() commentsModal: boolean;
  @Input() commentsLabel: string;
  @Input() showIcon: boolean;
  @Input() iconName: string;
  @Input() dialogTitle: string;
  @Input() dialogText: string;
  @Input() acceptBtnTittle: string;
  @Input() cancelBtnTittle: string;

  constructor(private matDialog: MatDialog) {
  }

  openStandardModal(): void {
    const dialogRef = this.matDialog.open(ConfirmationDialogComponent);
    dialogRef.componentInstance.iconName = this.iconName;
    dialogRef.componentInstance.dialogTitle = this.dialogTitle;
    dialogRef.componentInstance.dialogTitle = this.dialogTitle;
    dialogRef.componentInstance.dialogText = this.dialogText;
    dialogRef.componentInstance.acceptBtnTittle = this.acceptBtnTittle;
    dialogRef.componentInstance.cancelBtnTittle = this.cancelBtnTittle;
    dialogRef.afterClosed().subscribe(result => {
      console.log('Confirmation Dialog Closed with result:', result);
    });
  }

  openCommentsModal(commentsDialog: TemplateRef<any>): void {
    const dialogRef = this.matDialog.open(commentsDialog, { width: '600px' });
    dialogRef.afterClosed().subscribe(result => {
      console.log('Confirmation Dialog Closed with result:', result);
    });
  }
}

export const primary = () => ({
  moduleMetadata: {
    declarations: [ConfirmationWrapperComponent],
    imports: [BrowserAnimationsModule, TouchableComponentsModule, MaterialModule, RouterModule.forRoot([], { useHash: true }),
      TranslateModule.forRoot({
        loader: {
          provide: TranslateLoader,
          useClass: TranslateFakeLoader
        }
      })]
  },
  component: ConfirmationWrapperComponent,
  props: {
    showIcon: boolean('showIcon', true),
    iconName: text('iconName', 'dripicons:warning'),
    dialogTitle: text('dialogTitle', 'Confirmation Dialog Title'),
    dialogText: text('dialogText', 'This is a warning confirmation text, do you want to continue?'),
    acceptBtnTittle: text('acceptBtnTittle', 'Yes'),
    cancelBtnTittle: text('cancelBtnTittle', 'No'),
    commentsModal: boolean('commentsModal', false)
  }
});

export const commentsModal = () => ({
  moduleMetadata: {
    declarations: [ConfirmationWrapperComponent],
    imports: [BrowserAnimationsModule, TouchableComponentsModule, MaterialModule, RouterModule.forRoot([], { useHash: true }),
      TranslateModule.forRoot({
        loader: {
          provide: TranslateLoader,
          useClass: TranslateFakeLoader
        }
      })]
  },
  component: ConfirmationWrapperComponent,
  props: {
    dialogTitle: text('dialogTitle', 'Dialog Title'),
    acceptBtnTittle: text('acceptBtnTittle', 'Submit'),
    cancelBtnTittle: text('cancelBtnTittle', 'Cancel'),
    commentsModal: boolean('commentsModal', true),
    commentsLabel: text('commentsLabel', 'Comments')
  }
});
