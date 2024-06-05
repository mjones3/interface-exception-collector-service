import { Component, OnInit } from '@angular/core';
import { ShipmentInfoDto } from '@rsa/commons';
import { Observable } from 'rxjs';
import { PrintService } from '@rsa/distribution/core/print/print.service';

@Component({
  selector: 'rsa-view-pick-list',
  templateUrl: './view-pick-list.component.html',
})
export class ViewPickListComponent implements OnInit {
  model$: Observable<ShipmentInfoDto>;

  constructor(
    private printService: PrintService
  ) {}

  ngOnInit(): void {}

  hasAnyShortDate(model: ShipmentInfoDto): boolean {
    return !!model?.items?.some(i => i.shortDateProducts?.length);
  }

  print(): void {
    this.printService.print('viewPickListReport', { pagesize: 'A4 portrait' });
  }

}
