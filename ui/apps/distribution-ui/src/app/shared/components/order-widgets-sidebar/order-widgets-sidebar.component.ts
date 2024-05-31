import { Component, Input, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Description, ProcessHeaderService } from '@rsa/commons';

@Component({
  selector: 'rsa-order-widget-sidebar',
  templateUrl: './order-widgets-sidebar.component.html',
  styleUrls: ['./order-widgets-sidebar.component.scss'],
})
export class OrderWidgetsSidebarComponent implements OnInit {
  @Input() comments: string;
  @Input() orderInfoDescriptions: Description[] = [];
  @Input() billInfoDescriptions: Description[] = [];
  @Input() shippingInfoDescriptions: Description[] = [];
  @Input() feesInfoDescriptions: Description[] = [];
  @Input() hasContentOrNot: boolean;

  constructor(
    protected router: Router,
    protected activeRoute: ActivatedRoute,
    protected headerService: ProcessHeaderService
  ) {}

  ngOnInit(): void {}
}
