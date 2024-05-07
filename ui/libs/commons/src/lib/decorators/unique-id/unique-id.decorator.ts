import {uniqueId} from 'lodash';

export function UniqueId(prefix = '') {
  return (target: any, key: string) => {
    Object.defineProperty(target, key, {
      value: uniqueId(prefix),
      writable: false,
      enumerable: true,
      configurable: true
    });
  };
}
