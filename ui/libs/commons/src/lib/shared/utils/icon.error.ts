export const ICON_ANGULAR_CONSOLE_PREFIX = '[@rsa/theme]:';

export const error = (message: string): void => {
  console.error(`${ICON_ANGULAR_CONSOLE_PREFIX} ${message}.`);
};

export function NameSpaceIsNotSpecifyError(): Error {
  return new Error(`${ICON_ANGULAR_CONSOLE_PREFIX}Type should have a namespace. Try "namespace:${name}".`);
}

export const IconNotFoundError = (icon: string): Error => {
  return new Error(`${ICON_ANGULAR_CONSOLE_PREFIX}the icon ${icon} does not exist or is not registered.`);
};

export const HttpModuleNotImport = (): null => {
  error(`you need to import "HttpClientModule" to use dynamic importing.`);
  return null;
};

export const UrlNotSafeError = (url: string): Error => {
  return new Error(`${ICON_ANGULAR_CONSOLE_PREFIX}The url "${url}" is unsafe.`);
};

export const SVGTagNotFoundError = (): Error => {
  return new Error(`${ICON_ANGULAR_CONSOLE_PREFIX}<svg> tag not found.`);
};

export const DynamicLoadingTimeoutError = (): Error => {
  return new Error(`${ICON_ANGULAR_CONSOLE_PREFIX}Importing timeout error.`);
};
