import { Component } from '@angular/core';
import { EnvironmentConfigService } from '@rsa/commons';

@Component({
  selector: 'rsa-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
export class AppComponent {
  title = 'distribution-ui';
  environment: string;

  constructor(private config: EnvironmentConfigService) {
    this.environment = config.env.environment;
  }
}
