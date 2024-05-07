import { configure, addDecorator } from '@storybook/angular';
import { withKnobs } from '@storybook/addon-knobs';
import '!style-loader!css-loader!sass-loader!../../shared/styles/src/lib/vendors.scss';
import '!style-loader!css-loader!sass-loader!../../shared/styles/src/lib/main.scss';
import '!style-loader!css-loader!sass-loader!../../shared/styles/src/lib/tailwind/admin/tailwind.scss';
import '!style-loader!css-loader!../../shared/assets/src/fonts/inter/inter.css';
import '!style-loader!css-loader!sass-loader!../../../node_modules/simple-keyboard/build/css/index.css';

addDecorator(withKnobs);
configure(require.context('../src/lib', true, /\.stories\.(j|t)sx?$/), module);
