# Global Message Component

## How to use the component?

### Import Module

import {GlobalMessageComponent} from "@shared";

### HTML

```html
<global-message [messageType]="success" [message]="message"></global-message>
<global-message [messageType]="warning" [message]="message"></global-message>
<global-message [messageType]="error" [message]="message"></global-message>
```

### TS

```ts
messageType = FuseAlertType;
```

### Title

Should only be passed if you need to use a title different from the default ones (Warning, Caution, Success, System).

### Dismissable

As default the messages are not dismissable you have to pass `[dismissible]="true"` to make it dismissible.

### Dismiss Event

You can listen to dismissed event if any logic needed after dismissing it.

```html
<global-message (dismissed)="event()"></global-message>
```
