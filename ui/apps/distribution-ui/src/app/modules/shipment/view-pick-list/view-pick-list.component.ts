import { Component, OnInit } from '@angular/core';
import { ShipmentInfoDto } from '@rsa/commons';
import { Observable } from 'rxjs';

@Component({
  selector: 'rsa-view-pick-list',
  templateUrl: './view-pick-list.component.html',
})
export class ViewPickListComponent implements OnInit {
  model$: Observable<ShipmentInfoDto>;

  constructor() {}

  ngOnInit(): void {}

  hasAnyShortDate(model: ShipmentInfoDto): boolean {
    return !!model?.items?.some(i => i.shortDateProducts?.length);
  }
}
