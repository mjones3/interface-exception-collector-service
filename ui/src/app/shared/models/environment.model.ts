import { ProcessProductVersionModel } from './process-product-version.model';

export interface Environment {
    serverApiURL: string;
    agentApiURL: string;
    properties: Map<string, string>;
    productVersion: ProcessProductVersionModel;
    realm: string;
    clientId: string;
    url: string;
    environment: string;
    uuid: string;
}

