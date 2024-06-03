import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { ShipmentInfoDto } from '@rsa/commons';

@Component({
  selector: 'rsa-view-pick-list',
  templateUrl: './view-pick-list.component.html',
  styleUrls: ['./view-pick-list.component.scss']
})
export class ViewPickListComponent implements OnInit {

  model$: Observable<ShipmentInfoDto>;

  constructor() {}

  ngOnInit(): void {
  }

  hasAnyShortDate(model: ShipmentInfoDto): boolean {
    return !!model?.items?.some(i => i.shortDateProducts?.length);
  }

}
