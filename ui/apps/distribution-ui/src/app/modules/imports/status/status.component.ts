import { Component, Input, OnInit } from '@angular/core';
import { ImportItemDto } from '@rsa/commons';

@Component({
  selector: 'rsa-import-status-modal',
  templateUrl: './status.component.html',
  styleUrls: ['./status.component.scss'],
})
export class ImportStatusModalComponent implements OnInit {
  @Input() quantity = 0;
  @Input() processed = 0;
  @Input() failures: ImportItemDto[] = [];
  @Input() canClose: boolean;

  constructor() {}

  ngOnInit(): void {}

  get failedProductCodes() {
    return this.failures
      ?.map(failure => {
        return `${failure.unitNumber} - ${failure.productCode}`;
      })
      .join(',');
  }
}
