export interface ThemeDefinition {
    theme?: ThemeType;
    themeSuffix?: ThemeTypeUpperCase;
}

export interface IconDefinition extends ThemeDefinition {
    name: string; // kebab-case-style
    theme?: ThemeType;
    icon: string;
}

export type ThemeType = 'rsa' | 'hi_outline' | 'hi_solid' | 'dripicons';
export type ThemeTypeUpperCase =
    | 'Rsa'
    | 'HeroiconsOutline'
    | 'HeroiconsSolid'
    | 'Dripicons';
