import { Component, Input, OnInit } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { Observable, of } from 'rxjs';
import {
  DeepPartial,
  ShipmentDetailResponseDTO,
  VIEW_PICK_LIST_MOCK,
} from '@rsa/distribution/modules/orders/view-pick-list/view-pick-list.mock';

@Component({
  selector: 'rsa-view-pick-list',
  templateUrl: './view-pick-list.component.html',
  styleUrls: ['./view-pick-list.component.scss']
})
export class ViewPickListComponent implements OnInit {

  @Input("model") model$: Observable<DeepPartial<ShipmentDetailResponseDTO>>;

  constructor(
    private matDialogRef: MatDialogRef<ViewPickListComponent, DeepPartial<ShipmentDetailResponseDTO>>
  ) {
    // FIXME Remove this mock after integration with API
    this.model$ = of(VIEW_PICK_LIST_MOCK);
  }

  ngOnInit(): void {
  }

}
