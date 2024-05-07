/**
 * Default configuration for the Treo Theme
 *
 * If you need to store global configuration for your app, you can use this
 * object to set the defaults. To access, update and reset the config, use
 * 'AppConfigService'.
 */
import { ModuleConfig } from '@rsa/commons';

export const appConfig: ModuleConfig = {
  processUUID: '215b898c-0f1e-43e8-abd1-f835bef767e3',
  appTreoConfig: {
    name: 'Distribution App',
    description: 'Distribution Application',
    layout: 'basic',
    theme: 'distribution-light',
  },
};
