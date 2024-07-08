import { Component } from '@angular/core';
import { Observable } from 'rxjs';
import { MatIcon } from '@angular/material/icon';
import { AsyncPipe } from '@angular/common';
import { MatIconButton } from '@angular/material/button';
import { MatDialogClose } from '@angular/material/dialog';
import { BrowserPrintingService } from '../../../core/services/browser-printing/browser-printing.service';
import { DEFAULT_PAGE_SIZE } from '../../../core/services/browser-printing/browser-printing.model';
import { ShipmentInfoDto } from '../models/shipment-info.dto';

@Component({
  selector: 'app-view-pick-list',
  standalone: true,
    imports: [
        MatIcon,
        MatIconButton,
        MatDialogClose,
        AsyncPipe,
    ],
  templateUrl: './view-pick-list.component.html',
})
export class ViewPickListComponent {

  model$: Observable<ShipmentInfoDto>;

  constructor(
    private browserPrintingService: BrowserPrintingService
  ) {}

  hasAnyShortDate(model: ShipmentInfoDto): boolean {
    return !!model?.items?.some(i => i.shortDateProducts?.length);
  }

  print(): void {
    this.browserPrintingService.print('viewPickListReport', { pageSize: DEFAULT_PAGE_SIZE });
  }

}
