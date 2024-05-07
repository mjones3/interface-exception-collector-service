import { Type } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { createTestContext, TestContext } from '@rsa/testing';
import { APP_ICONS_TOKEN } from '../../core/startup/default-startup.loader';
import { IconDefinition } from '../../shared/models/icon-types.model';
import { IconService } from '../../shared/services';

export const addRsaIconsMock = (testContext: TestContext<any>, icons: IconDefinition[] = []): void => {
  const iconService = testContext.resolve(IconService);
  iconService.addIcon(...icons);
  iconService.addIconSet('assets/icons/material-outline.svg');
};

/**
 * Get app icon token for testing
 * @example
 *  beforeEach(async () => {
 *     await TestBed.configureTestingModule({
 *       declarations: [],
 *       imports: [],
 *       providers: [
 *         getAppIconsTokenMockProvider(allDistributionIcons)
 *       ]
 *     })
 *       .compileComponents();
 *   });
 * @param icons
 */
export const getAppIconsTokenMockProvider = (icons: IconDefinition[]) => {
  return { provide: APP_ICONS_TOKEN, useValue: [...icons] };
};

export const createTestContextAndIcons = <T>(component: Type<T>): TestContext<T> => {
  const testContext = createTestContext<T>(component);
  const iconsTokenData = TestBed.inject(APP_ICONS_TOKEN) || [];
  addRsaIconsMock(testContext, iconsTokenData);
  return testContext;
};
