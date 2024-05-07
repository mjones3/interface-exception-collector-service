import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ProcessHeaderService } from '../../shared/services/process-header.service';
import { BaseProcessComponent } from '../base-process/base-process.component';

@Component({
  selector: 'rsa-base-process-layout',
  templateUrl: './base-process-layout.component.html',
})
export class BaseProcessLayoutComponent extends BaseProcessComponent implements OnInit {
  constructor(
    protected router: Router,
    protected activeRoute: ActivatedRoute,
    public headerService: ProcessHeaderService
  ) {
    super(router, activeRoute, headerService);
  }

  ngOnInit(): void {
    super.init();
  }
}
