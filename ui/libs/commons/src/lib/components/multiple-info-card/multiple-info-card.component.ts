import { Component, Input, OnInit, TemplateRef } from '@angular/core';
import { MultipleInfoData } from '../../shared/models/card-info.model';

@Component({
  selector: 'rsa-multiple-info-card',
  templateUrl: './multiple-info-card.component.html',
})
export class MultipleInfoCardComponent implements OnInit {
  @Input() infoData: MultipleInfoData[];
  @Input() title;
  @Input() subtitle;
  @Input() menuTitle;
  @Input() hasMenu = false;
  @Input() subMenu = false;

  @Input() menus?: { label: string; click: Function; icon?: string }[];
  @Input() templateRef: TemplateRef<any>;
  constructor() {}

  ngOnInit(): void {}
}
