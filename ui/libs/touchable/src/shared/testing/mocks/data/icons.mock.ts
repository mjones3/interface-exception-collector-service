import { TestContext } from '@rsa/testing';
import { IconService } from '@rsa/commons';
import { DRIP_ICONS, HEROIC_ICONS, RSA_ICONS } from '../../../../lib/icons';

export const addTestingIconsMock = (testContext: TestContext<any>): void => {
  const iconService = testContext.resolve(IconService);
  iconService.addIcon(...RSA_ICONS, ...HEROIC_ICONS, ...DRIP_ICONS);
  iconService.addIconSet('assets/icons/material-outline.svg');
};
