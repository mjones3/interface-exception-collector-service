import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterModule } from '@angular/router';
import { TouchableComponentsModule } from '../touchable-components.module';
import { OnScreenKeyboardComponent } from './on-screen-keyboard.component';

export default {
  title: 'rsa-on-screen-keyboard',
};

export const primary = () => ({
  moduleMetadata: {
    imports: [
      BrowserAnimationsModule,
      TouchableComponentsModule,
      RouterModule.forRoot([], { useHash: true }),
    ],
  },
  component: OnScreenKeyboardComponent,
  props: {},
});
