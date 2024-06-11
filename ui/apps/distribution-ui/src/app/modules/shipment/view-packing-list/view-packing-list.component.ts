import { Component, OnInit } from '@angular/core';
import { Observable, of } from 'rxjs';
import { PackingListLabelDTO } from '@rsa/commons';

@Component({
  selector: 'rsa-view-packing-list',
  templateUrl: './view-packing-list.component.html',
})
export class ViewPackingListComponent implements OnInit {

  model$: Observable<Partial<PackingListLabelDTO>> = of();

  constructor() {}

  ngOnInit(): void {}

  getBase64DataImage(payload: string): string {
    return `data:image/*;base64,${payload}`;
  }

}
