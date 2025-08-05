/**
 * From NetanelBasal/ngx-auto-unsubscribe and update it
 */
import {isFunction} from 'lodash';
import {Subscription} from 'rxjs';

const doUnsubscribe = subscription => {
  if (subscription && subscription instanceof Subscription) {
    subscription.unsubscribe();
  }
};

const doUnsubscribeIfArray = subscriptionsArray => {
  if (Array.isArray(subscriptionsArray)) {
    subscriptionsArray.forEach(doUnsubscribe);
  }
};

export function AutoUnsubscribe({blackList = [], arrayName = '', eventFn = 'ngOnDestroy'} = {}) {
  return (target) => {
    const original = target.prototype[eventFn];
    target.prototype[eventFn] = function () {
      if (isFunction(original)) {
        original.apply(this, arguments);
      }
      if (arrayName) {
        doUnsubscribeIfArray(this[arrayName]);
        return;
      }
      for (const propName in this) {
        if (this.hasOwnProperty(propName)) {
          if (blackList.includes(propName)) {
            continue;
          }
          const property = this[propName];
          doUnsubscribe(property);
        }
      }
    };
  };
}
