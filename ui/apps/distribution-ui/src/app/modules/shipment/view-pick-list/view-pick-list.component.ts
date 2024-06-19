import { Component, OnInit } from '@angular/core';
import { ShipmentInfoDto } from '@rsa/commons';
import { Observable } from 'rxjs';
import { BrowserPrintingService } from '@rsa/distribution/core/print-section/browser-printing.service';

@Component({
  selector: 'rsa-view-pick-list',
  templateUrl: './view-pick-list.component.html',
})
export class ViewPickListComponent implements OnInit {
  model$: Observable<ShipmentInfoDto>;

  constructor(
    private browserPrintService: BrowserPrintingService
  ) {}

  ngOnInit(): void {}

  hasAnyShortDate(model: ShipmentInfoDto): boolean {
    return !!model?.items?.some(i => i.shortDateProducts?.length);
  }

  print(): void {
    this.browserPrintService.print('viewPickListReport', { pagesize: 'A4 portrait' });
  }

}
