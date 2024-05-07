import { Component, EventEmitter, Input, Output, TemplateRef } from '@angular/core';
import { Menu } from '../../shared/models/widget.model';

@Component({
  selector: 'rsa-common-widget',
  templateUrl: './widget.component.html',
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
  @Input() buttonMenu: string;

  @Output() menuClick: EventEmitter<Menu> = new EventEmitter<Menu>();

  onMenuClick(menu: Menu) {
    // Keep backward compatibility with click function
    if (menu && menu.click) {
      menu.click();
    }
    this.menuClick.emit(menu);
  }
}
