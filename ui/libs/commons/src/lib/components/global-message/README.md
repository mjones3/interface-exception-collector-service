# Global Message Component

##How to use the component?

###Import Module
import {RsaCommonsModule} from "@rsa/commons";

###Add Icons
Add Icons to the icons.ts of the module you want to add the component:

ErrorMessageRsa, BlockMessageRsa, SuccessMessageRsa, WarningMessageRsa, PrimaryMessageRsa

###HTML

<global-message [messageType]="messageType.success"></global-message>

<global-message [messageType]="messageType.warning"></global-message>

<global-message [messageType]="messageType.error"></global-message>

###TS

messageType = MessageType;

### Primary Message

This is the only global message you can customize. You can leave it as is or change their Title, Message, Colors, Icon.

### Closable

As default the messages are not closable you have to pass `[closable]="true"` to make it closable.
