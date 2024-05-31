import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import {
  DeepPartial,
  ShipmentDetailResponseDTO,
} from '@rsa/distribution/modules/orders/view-pick-list/view-pick-list.mock';

@Component({
  selector: 'rsa-view-pick-list',
  templateUrl: './view-pick-list.component.html',
  styleUrls: ['./view-pick-list.component.scss']
})
export class ViewPickListComponent implements OnInit {

  model$: Observable<DeepPartial<ShipmentDetailResponseDTO>>;

  constructor() {}

  ngOnInit(): void {
  }

  hasAnyShortDate(model: DeepPartial<ShipmentDetailResponseDTO>): boolean {
    return !!model?.items?.some(i => i.shortDateProducts?.length);
  }

}
