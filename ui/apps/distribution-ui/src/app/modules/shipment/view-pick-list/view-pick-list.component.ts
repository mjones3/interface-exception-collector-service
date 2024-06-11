import { Component, OnInit } from '@angular/core';
import { ShipmentInfoDto } from '@rsa/commons';
import { Observable } from 'rxjs';
import { BrowserPrintService } from '@rsa/distribution/core/print-section/browser-print.service';

@Component({
  selector: 'rsa-view-pick-list',
  templateUrl: './view-pick-list.component.html',
})
export class ViewPickListComponent implements OnInit {
  model$: Observable<ShipmentInfoDto>;

  constructor(
    private browserPrintService: BrowserPrintService
  ) {}

  ngOnInit(): void {}

  hasAnyShortDate(model: ShipmentInfoDto): boolean {
    return !!model?.items?.some(i => i.shortDateProducts?.length);
  }

  print(): void {
    this.browserPrintService.print('viewPickListReport', { pagesize: 'A4 portrait' });
  }

}
