export interface Translation {
  [key: string]: Translation | string;
}

export const TRANSLATIONS: Readonly<Partial<Translation>> = Object.freeze({});
