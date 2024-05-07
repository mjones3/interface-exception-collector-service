import camelCase from 'lodash.camelcase';
import upperFirst from 'lodash.upperfirst';
import {pipe} from 'ramda';
import {ThemeDefinition, ThemeType, ThemeTypeUpperCase} from '../templates/types';

export interface IdentifierMeta {
  name: string;
  themeSuffix?: ThemeTypeUpperCase;
}

export type GetIdentifierType = (meta: IdentifierMeta) => string;

export const getIdentifier: GetIdentifierType = pipe(
  ({name, themeSuffix}: IdentifierMeta) =>
    name + (themeSuffix ? `-${themeSuffix}` : ''),
  camelCase,
  upperFirst
);

export const rsaThemeDef: ThemeDefinition = {
  theme: 'rsa',
  themeSuffix: 'Rsa'
};

export const heroiconsOutlineThemeDef: ThemeDefinition = {
  theme: 'hi_outline',
  themeSuffix: 'HeroiconsOutline'
};

export const heroiconsSolidThemeDef: ThemeDefinition = {
  theme: 'hi_solid',
  themeSuffix: 'HeroiconsSolid'
};

export const dripiconsThemeDef: ThemeDefinition = {
  theme: 'dripicons',
  themeSuffix: 'Dripicons'
};

export const themeDefinitions: ThemeDefinition[] = [
  rsaThemeDef,
  heroiconsOutlineThemeDef,
  heroiconsSolidThemeDef,
  dripiconsThemeDef
];

export const getThemeByPath = (path: string) => {
  let theme: ThemeType = rsaThemeDef.theme;
  if (path.search(heroiconsOutlineThemeDef.theme as string) > 0) {
    theme = heroiconsOutlineThemeDef.theme;
  } else if (path.search(heroiconsSolidThemeDef.theme as string) > 0) {
    theme = heroiconsSolidThemeDef.theme;
  } else if (path.search(dripiconsThemeDef.theme as string) > 0) {
    theme = dripiconsThemeDef.theme;
  }
  return theme;
};
