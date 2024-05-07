import { TestContext } from '@rsa/testing';
import { IconService } from '@rsa/commons';
import { DRIP_ICONS, HEROIC_ICONS, RSA_ICONS } from '../../../icons';

export const addRsaIconsMock = (testContext: TestContext<any>): void => {
  const iconService = testContext.resolve(IconService);
  iconService.addIcon(...RSA_ICONS, ...DRIP_ICONS, ...HEROIC_ICONS);
  iconService.addIconSet('assets/icons/material-outline.svg');
};
