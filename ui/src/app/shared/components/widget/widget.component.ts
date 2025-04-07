import { CommonModule } from '@angular/common';
import {
    Component,
    EventEmitter,
    Input,
    Output,
    TemplateRef,
} from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatMenu } from '@angular/material/menu';
import { Menu } from 'app/shared/models/widget.model';
import { BasicButtonComponent } from '../buttons/basic-button.component';

@Component({
    standalone: true,
    selector: 'rsa-common-widget',
    templateUrl: './widget.component.html',
    imports: [CommonModule, MatIconModule, MatMenu, BasicButtonComponent],
})
export class WidgetComponent {
    @Input() title: string;
    @Input() subtitle: string;
    @Input() svgIcon: string;
    @Input() svgIconCls: string;
    @Input() color: string;
    @Input() hasMenu = false;
    @Input() hasBorder = false;
    @Input() menus: Menu[];
    @Input() templateRef: TemplateRef<any>;
    @Input() showBasicButton = false;
    @Input() buttonLabel?: string;
    @Input() buttonIcon?: string;
    @Input() buttonDisabled?: boolean = false;
    @Input() buttonColor?: 'primary' | 'secondary' = 'primary';
    @Input() buttonClass?: string;
    @Input() buttonId?: string = 'widget-button';
    @Input() actionIcon: string;
    @Input() actionButtonLabel: string;
    @Input() btnClass: string;
    @Output() buttonClick: EventEmitter<string> = new EventEmitter<string>();
    @Output() iconButtonClick: EventEmitter<string> =
        new EventEmitter<string>();
    @Output() menuClick: EventEmitter<Menu> = new EventEmitter<Menu>();

    onMenuClick(menu: Menu) {
        // Keep backward compatibility with click function
        if (menu && menu.click) {
            menu.click();
        }
        this.menuClick.emit(menu);
    }

    onActionClick(): void {
        this.iconButtonClick.emit();
    }

    onButtonClick(): void {
        this.buttonClick.emit();
    }
}
