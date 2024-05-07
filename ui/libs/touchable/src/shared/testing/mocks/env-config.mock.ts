import { Environment, EnvironmentConfigService } from '@rsa/commons';

export const envConfigFactoryMock = () => {
  const config = new EnvironmentConfigService();
  config.env = {
    serverApiURL: 'http://localhost:4200',
    url: 'https://id.dev.ao.arc-one.com/auth',
    properties: {},
  } as Environment;
  return config;
};
