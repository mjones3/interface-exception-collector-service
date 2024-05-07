import { ChangeDetectionStrategy, Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { Description } from '../../shared/models/description.model';

export type descriptionCardLayout = 'vertical' | 'horizontal';

@Component({
  selector: 'rsa-description-card',
  exportAs: 'rsaDescriptionCard',
  templateUrl: './description-card.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  styleUrls: ['./description-card.component.scss'],
})
export class DescriptionCardComponent implements OnChanges {
  @Input() embedded = false;
  @Input() labelClasses = 'gt-xs:min-w-30';
  @Input() valueClasses = 'break-all';
  @Input() title;
  @Input() showColon = false;
  @Input() maxRows = 4;
  @Input() maxCols = 3;
  @Input() descriptions: Description[] = [];
  @Input() layout: descriptionCardLayout = 'vertical';
  @Input() image: string;
  @Input() imageType: 'svgIcon' | 'img' = 'svgIcon';
  @Input() imageCls = 'w-20 h-20';
  @Input() gapCls = 'gap-2';

  descriptionsGroup: Description[][] = [];
  columns = 1;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.descriptions && changes.descriptions.currentValue) {
      this.descriptions = changes.descriptions.currentValue;
      this.orderDescriptionsByLayout();
    }
  }

  private orderDescriptionsByLayout() {
    if (this.layout === 'vertical') {
      this.orderDescriptionsByColumns();
    } else if (this.layout === 'horizontal') {
      this.orderDescriptionsByRows();
    }
  }

  private orderDescriptionsByColumns() {
    this.descriptionsGroup = [];
    if (this.descriptions.length > this.maxRows * this.maxCols) {
      // this.maxRows = this.descriptions.length % 2 === 0 ? Math.floor(this.descriptions.length / 2) : Math.floor(this.descriptions.length / 2) + 1;
      this.maxRows = Math.ceil(this.descriptions.length / this.maxCols);
    }
    this.columns = Math.ceil(this.descriptions.length / this.maxRows);
    for (let i = 0; i < this.descriptions.length; i++) {
      const index = i < this.maxRows ? i : i % this.maxRows;
      this.descriptionsGroup[index] = [...(this.descriptionsGroup[index] || []), this.descriptions[i]];
    }
  }

  private orderDescriptionsByRows() {
    this.descriptionsGroup = [];
    this.columns = this.maxCols;
    for (let i = 0; i < this.descriptions.length; i++) {
      const index = Math.floor(i / this.maxCols);
      this.descriptionsGroup[index] = [...(this.descriptionsGroup[index] || []), this.descriptions[i]];
    }
  }
}
