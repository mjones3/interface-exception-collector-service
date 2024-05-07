import { APP_INITIALIZER, InjectionToken } from '@angular/core';
import { Environment } from '../../shared/models';
import { EnvironmentConfigService, IconService } from '../../shared/services';

const defaultPropertiesMap = new Map<string, any>();
defaultPropertiesMap.set('message-timeout-short', 3000);
defaultPropertiesMap.set('message-timeout', 20000);

export const envConfigFactoryMock = (clientId: string, propertiesMap: Map<string, any> = defaultPropertiesMap) => {
  const config = new EnvironmentConfigService();
  config.env = {
    serverApiURL: 'http://localhost:4200',
    url: 'https://id.dev.ao.arc-one.com/auth',
    realm: 'rsa',
    clientId,
    properties: propertiesMap,
  } as Environment;
  return config;
};

function configurationFactory(
  icon: IconService,
  config: EnvironmentConfigService,
  clientId: string,
  icons: any[],
  propertiesMap: Map<string, any> = defaultPropertiesMap
): () => Promise<any> {
  return (): Promise<any> => {
    return new Promise((resolve, _reject) => {
      config.env = {
        production: false,
        serverApiURL: 'http://localhost:4200',
        url: 'https://id.dev.ao.arc-one.com/auth',
        realm: 'rsa',
        clientId: clientId,
        properties: propertiesMap,
      } as Environment;

      // Add additional loaders
      icon.addIcon(...icons);
      resolve(true);
    });
  };
}

export const getAppInitializerMockProvider = (
  clientId: string,
  icons: any[] = [],
  propertiesMap: Map<string, any> = defaultPropertiesMap
) => {
  const clientIdToken = new InjectionToken('clientId', {
    providedIn: 'root',
    factory: () => clientId,
  });
  const propertiesMapToken = new InjectionToken('clientId', {
    providedIn: 'root',
    factory: () => propertiesMap,
  });
  const iconToken = new InjectionToken('iconToken', {
    providedIn: 'root',
    factory: () => icons || [],
  });
  return [
    {
      provide: APP_INITIALIZER,
      useFactory: configurationFactory,
      multi: true,
      deps: [IconService, EnvironmentConfigService, clientIdToken, iconToken, propertiesMapToken],
    },
  ];
};
