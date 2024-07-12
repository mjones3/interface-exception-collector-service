import { CommonModule } from '@angular/common';
import { Component, Input, OnInit } from '@angular/core';
import { MatExpansionModule } from '@angular/material/expansion';
import { ActivatedRoute, Router } from '@angular/router';
import { DescriptionCardComponent } from 'app/shared/components/information-card/description-card.component';
import { WidgetComponent } from 'app/shared/components/widget/widget.component';
import { Description } from 'app/shared/models/description.model';
import { ProcessHeaderService } from 'app/shared/services/process-header.service';

@Component({
  standalone: true,
  selector: 'rsa-order-widget-sidebar',
  templateUrl: './order-widgets-sidebar.component.html',
  styleUrls: ['./order-widgets-sidebar.component.scss'],
  imports: [
    CommonModule,
    WidgetComponent,
    DescriptionCardComponent,
    MatExpansionModule
  ]
})
export class OrderWidgetsSidebarComponent implements OnInit {
  @Input() comments: string;
  @Input() orderInfoDescriptions: Description[] = [];
  @Input() shippingInfoDescriptions: Description[] = [];
 

  constructor(
    protected router: Router,
    protected activeRoute: ActivatedRoute,
    protected headerService: ProcessHeaderService
  ) {}

  ngOnInit(): void {}
}
