import { ProcessProductVersionDto } from './process-product-version.dto';

export interface Environment {
  production: boolean;
  module: string;
  serverApiURL: string;
  url: string;
  realm: string;
  clientId: string;
  environment: string;
  properties: Map<string, string>;
  uuid: string;
  productVersion: ProcessProductVersionDto;
  adminUrl?: string;
  loadUserProfileAtStartUp?: boolean;
  [key: string]: any;
}
