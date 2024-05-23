import { HttpResponse } from '@angular/common/http';
import { ProcessProductDto, ProcessProductVersionDto } from '@rsa/commons';

export const PROCESS_CONFIGURATION: HttpResponse<ProcessProductDto> = {
  body: {
    id: '215b898c-0f1e-43e8-abd1-f835bef767e3',
    descriptionKey: 'process-product.distribution.label',
    active: true,
    orderNumber: 26,
    properties: {
      'arc-default-logo': 'assets/images/logo/ARC-logo.PNG',
      release_number: 'v1.0-30.01.11282023',
    },
    isModule: true,
  } as any,
} as HttpResponse<ProcessProductDto>;

export const PROCESS_PRODUCT_VERSION = {
  body: {
    id: 3,
    productId: '215b898c-0f1e-43e8-abd1-f835bef767e3',
    createDate: '2022-11-04T15:29:48.112Z',
    releaseVersion: null,
    buildVersion: '23.6.1278',
    releaseNotes: null,
  } as any,
} as HttpResponse<ProcessProductVersionDto>;
