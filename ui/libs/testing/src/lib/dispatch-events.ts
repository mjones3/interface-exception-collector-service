/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */

import { createFakeEvent, createKeyboardEvent, createMouseEvent, createTouchEvent } from './event-objects';

/** Utility to dispatch any event on a Node. */
export const dispatchEvent = (node: Node | Window, event: Event): Event => {
  node.dispatchEvent(event);
  return event;
};

/** Shorthand to dispatch a fake event on a specified node. */
export const dispatchFakeEvent = (node: Node | Window, type: string | Event, canBubble?: boolean): Event => {
  return dispatchEvent(node, typeof type === 'string' ? createFakeEvent(type, canBubble) : type);
};

/** Shorthand to dispatch a keyboard event with a specified key code. */
export const dispatchKeyboardEvent = (node: Node, type: string, keyCode: number, target?: Element, key?: string): KeyboardEvent => {
  return dispatchEvent(node, createKeyboardEvent(type, keyCode, target, key)) as KeyboardEvent;
};

/** Shorthand to dispatch a mouse event on the specified coordinates. */
export const dispatchMouseEvent = (
  node: Node,
  type: string,
  x: number = 0,
  y: number = 0,
  event: MouseEvent = createMouseEvent(type, x, y)
): MouseEvent => {
  return dispatchEvent(node, event) as MouseEvent;
};

/** Shorthand to dispatch a touch event on the specified coordinates. */
export const dispatchTouchEvent = (node: Node, type: string, x: number = 0, y: number = 0): Event => {
  return dispatchEvent(node, createTouchEvent(type, x, y));
};

/**
 * Focuses an input, sets its value and dispatches
 * the `input` event, simulating the user typing.
 * @param value Value to be set on the input.
 * @param element Element onto which to set the value.
 */
export const typeInElement = (value: string, element: HTMLInputElement): void => {
  element.focus();
  element.value = value;
  dispatchFakeEvent(element, 'input');
};
