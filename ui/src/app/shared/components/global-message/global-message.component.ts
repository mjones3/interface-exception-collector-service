import {
    booleanAttribute,
    Component,
    EventEmitter,
    Input,
    OnInit,
    Output,
} from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import {
    FuseAlertAppearance,
    FuseAlertComponent,
    FuseAlertType,
} from '@fuse/components/alert';

@Component({
    selector: 'biopro-global-message',
    templateUrl: './global-message.component.html',
    standalone: true,
    imports: [FuseAlertComponent, MatIconModule],
})
export class GlobalMessageComponent implements OnInit {
    @Input() appearance: FuseAlertAppearance = 'soft';
    @Input({ required: true }) messageType: FuseAlertType;
    @Input() messageTitle: string;
    @Input({ required: true }) message: string;
    @Input({ transform: booleanAttribute }) dismissible = false;

    @Output() readonly dismissed: EventEmitter<void> = new EventEmitter<void>();

    ngOnInit() {
        if (!this.messageTitle) {
            switch (this.messageType) {
                case 'error':
                    this.messageTitle = 'Warning';
                    break;
                case 'warning':
                    this.messageTitle = 'Caution';
                    break;
                case 'success':
                    this.messageTitle = 'Success';
                    break;
                case 'info':
                    this.messageTitle = 'System';
                    break;
            }
        }
    }

    dismiss() {
        this.dismissed.emit();
    }
}
