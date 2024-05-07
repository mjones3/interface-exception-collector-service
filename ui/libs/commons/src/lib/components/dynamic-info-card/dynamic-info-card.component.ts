import { Component, Input, TemplateRef } from '@angular/core';
import { CardInfo, NGClass } from '../../shared/models/card-info.model';
import { Menu } from '../../shared/models/widget.model';

@Component({
  selector: 'rsa-dynamic-info-card',
  templateUrl: './dynamic-info-card.component.html',
  styleUrls: ['./dynamic-info-card.component.scss'],
})
export class DynamicInfoCardComponent {
  @Input() infoData: CardInfo[];
  @Input() title: string;
  @Input() subTitle: string;
  @Input() menuTitle: string;
  @Input() hasMenu = false;
  @Input() subMenu = false;
  @Input() menus?: { label: string; click: Function; icon?: string; subMenu?: Menu[] }[];
  @Input() templateRef: TemplateRef<any>;
  @Input() titleRef: TemplateRef<any>;
  @Input() hasTitleDivider = false;
  @Input() titleClass: NGClass; // added titleClass Input, to have customized css for the header of info card
  @Input() bodyClass: NGClass; // added bodyClass Input to have customized css for the body info card
  @Input() titleTextClass: NGClass; // for changing appearance of title text
  @Input() subTitleTextClass: NGClass; // for changing appearance of sub title text
  @Input() leftInfoTextContainer: NGClass; // for styling descriptionKey text
  @Input() rightInfoTextContainer: NGClass; // for styling value text
  @Input() rightCornerInfo: { label: string; value: string };
}
