export interface CustomTranslation {
  [key: string]: CustomTranslation | string;
}

export const CUSTOM_TRANSLATIONS: Readonly<Partial<CustomTranslation>> = Object.freeze({});
